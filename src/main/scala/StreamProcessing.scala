import Transaction.{TransactionKeySerializer, Transaction, TransactionValueDeserializer, TransactionValueSerializer}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._

object StreamProcessing {

  // Streaming Environment
  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  def writeToTopic(topic: String): Unit = {
    // Sink
    val kafkaSink: KafkaSink[Transaction] = KafkaSink.builder[Transaction]()
      .setBootstrapServers("localhost:9092")
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic(topic)
        .setValueSerializationSchema(new TransactionValueSerializer)
        .setKeySerializationSchema(new TransactionKeySerializer).build()
      ).build()

    // Source
    val kafkaSource: KafkaSource[Transaction] = KafkaSource.builder[Transaction]()
      .setBootstrapServers("localhost:9092")
      .setTopics("transactions")
      .setGroupId("events-group")
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new TransactionValueDeserializer)
      .build()


    val transactionStream: DataStream[Transaction] = env.fromSource(kafkaSource,
      WatermarkStrategy.noWatermarks(),
      "Kafka Source")

    // Filter
    val filteredTransactionStream: DataStream[Transaction] = transactionStream
      .keyBy(transaction => transaction.id)
      .process(new FraudProcessor)

    // Final Sink
    filteredTransactionStream.sinkTo(kafkaSink)
    filteredTransactionStream.print()
    env.execute()
  }


  def main(args: Array[String]): Unit = {
    writeToTopic("potential_fraud_transactions")
  }
}
