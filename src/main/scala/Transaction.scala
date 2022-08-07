import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala._

object Transaction {

  case class Transaction(id: Int, amount: Int, transaction_type: String)

  // Deserialize Value from Source
  class TransactionValueDeserializer extends DeserializationSchema[Transaction] {
    override def deserialize(messageAsByte: Array[Byte]): Transaction = {
      val message: String = new String(messageAsByte)
      val tokens: Array[String] = message.split(",")
      val id: Int = tokens(0).toInt
      val amount: Int = tokens(1).toInt
      val transaction_type: String = tokens(2)
      Transaction(id, amount, transaction_type)
    }

    override def isEndOfStream(nextElement: Transaction): Boolean = false

    override def getProducedType: TypeInformation[Transaction] = implicitly[TypeInformation[Transaction]]
  }

  // Serialize Key to Sink
  class TransactionKeySerializer extends SerializationSchema[Transaction] {
    override def serialize(key: Transaction): Array[Byte] =
      s"${key.id}".getBytes()
  }

  // Serialize Value to Sink
  class TransactionValueSerializer extends SerializationSchema[Transaction] {
    override def serialize(transaction: Transaction): Array[Byte] =
      s"${transaction.id},${transaction.amount},${transaction.transaction_type}".getBytes("UTF-8")
  }
}
