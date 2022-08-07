import Transaction.Transaction
import org.apache.flink.api.common.state.{AggregatingState, MapState, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector


class FraudProcessor extends KeyedProcessFunction[Int, Transaction, Transaction] {
  var stateIDMapping: MapState[String, Any] = _
  var countValue: ValueState[Long] = _
  var aggAvg: AggregatingState[Int, Double] = _


  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    countValue = getRuntimeContext.getState(
      new ValueStateDescriptor[Long]("events-counter", classOf[Long])
    )
  }

  // Logic for each Key
  override def processElement(transaction: Transaction,
                              ctx: KeyedProcessFunction[Int, Transaction, Transaction]#Context,
                              out: Collector[Transaction]): Unit = {

    // Handle the State here
    val nEventsForThisUserID = countValue.value()
    countValue.update(nEventsForThisUserID + 1)

    println(s"${transaction.id} with Count ${countValue.value()}")

    out.collect(transaction)
  }
}

// implicit converters (extension methods)

// Use Aggregating State instead
//stateEventsForUser.add(transaction)
//val currentEvents = stateEventsForUser.get().asScala.toList
//if (currentEvents.size > 3) {
//  stateEventsForUser.clear() // clearing is not done immediately
//  var sum: Double = 0
//  var matchedRule: String = null
//  var flag: Boolean = false
//  for (index <- currentEvents.slice(1, 4)) { //
//    stateEventsForUser.add(index)
//    sum += index.amount
//    if (matchedRule == null) {
//      matchedRule = index.transaction_type
//    } else if (matchedRule == index.transaction_type) {
//      flag = true
//    } else {
//      flag = false
//    }
//  }
//
//  val avg: Double = sum / 3
//  if (flag == true && avg > 60)
//    stateEventsForUser.clear()
//
//  println(s"User ${transaction.id}, with avg of ${avg} - [${currentEvents.slice(1,4).mkString(", ")}], 3 ${flag}")
//}
//
//out.collect(transaction)