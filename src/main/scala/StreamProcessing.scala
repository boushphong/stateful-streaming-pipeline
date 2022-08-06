import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}


object StreamProcessing {
  val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  implicit val typeInfo: TypeInformation[String] = TypeInformation.of(classOf[String])
  case class Payment(id: Int, amount: Int, payment_type: String)

  implicit val typeInfoPayment: TypeInformation[Payment] = TypeInformation.of(classOf[Payment])
  class PaymentDeserializer extends DeserializationSchema[Payment] {
    override def deserialize(messageAsByte: Array[Byte]): Payment = {
      val message = new String(messageAsByte)
      val tokens = message.split(",")
      val id = tokens(0).toInt
      val amount = tokens(1).toInt
      val payment_type = tokens(2)
      Payment(id, amount, payment_type)
    }

    override def isEndOfStream(nextElement: Payment): Boolean = false

    override def getProducedType: TypeInformation[Payment] = implicitly[TypeInformation[Payment]]
  }


  class PaymentSerializer extends SerializationSchema[Payment] {
    override def serialize(payment: Payment): Array[Byte] =
      s"${payment.id},${payment.amount},${payment.payment_type}".getBytes("UTF-8")
  }

  def writeToTopic(topic: String): Unit = {
    val kafkaSink: KafkaSink[Payment] = KafkaSink.builder[Payment]()
      .setBootstrapServers("localhost:9092")
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic(topic)
        .setValueSerializationSchema(new PaymentSerializer).build()
      ).build()

    val kafkaSource: KafkaSource[Payment] = KafkaSource.builder[Payment]()
      .setBootstrapServers("localhost:9092")
      .setTopics("transactions")
      .setGroupId("events-group")
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new PaymentDeserializer)
      .build()

    val paymentStream: DataStream[Payment] = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source")

    val filteredPaymentStream: DataStream[Payment] = paymentStream.filter(_.amount >= 98)

    filteredPaymentStream.sinkTo(kafkaSink)
    filteredPaymentStream.print()
    env.execute()
  }


  def main(args: Array[String]): Unit = {
    writeToTopic("potential_fraud_transactions")
  }
}
