import com.newrelic.scala.api.TraceOps.{trace, txn}

object TransactionAndSegmentExample extends App {

  txn {
    val i = 1
    val j = 2
    println(i + j)

    trace("statement segment") {
      val i = 1
      val j = 2
      println(i + j)
    }
    // A trace can also be used as an expression
    val x: Int = trace("expression segment") {
      val i = 1
      val j = 2
      i + j
    }
    println(x)

  }

}
