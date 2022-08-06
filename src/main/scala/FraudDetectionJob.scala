import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala._
import org.apache.flink.walkthrough.common.sink.AlertSink
import org.apache.flink.walkthrough.common.entity.Alert
import org.apache.flink.walkthrough.common.entity.Transaction
import org.apache.flink.walkthrough.common.source.TransactionSource

object FraudDetectionJob {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    implicit val typeInfoLong: TypeInformation[Long] = TypeInformation.of(classOf[Long])
    implicit val typeInfoTransaction: TypeInformation[Transaction] = TypeInformation.of(classOf[Transaction])
    implicit val typeInfoAlert: TypeInformation[Alert] = TypeInformation.of(classOf[Alert])
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val transactions: DataStream[Transaction] = env
      .addSource(new TransactionSource)
      .name("transactions")

    val alerts: DataStream[Alert] = transactions
      .keyBy(transaction => transaction.getAccountId)
      .process(new FraudDetector)
      .name("fraud-detector")

    alerts
      .addSink(new AlertSink)
      .name("send-alerts")

    alerts.print()

    env.execute("Fraud Detection")
  }
}