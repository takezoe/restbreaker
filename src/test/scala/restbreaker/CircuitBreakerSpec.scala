package restbreaker

import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import TestServer._

class CircuitBreakerSpec extends FunSuite with BeforeAndAfter {

  before {
    TestServer.start()
  }

  after {
    TestServer.stop()
  }

  test("disabled"){
    val client = new CircuitBreaker(new AHCServiceClient(), enabled = false)
    val request = Request.get("http://localhost:8080/test")
    val f = client.call[Result](request)
    val r = Await.result(f, Duration.Inf)
    assert(r == Result("OK"))
  }

  test("open and close"){
    val client = new CircuitBreaker(new AHCServiceClient(), enabled = true, callTimeout = 500, maxFailures = 3, resetTimeout = 1000)
    for(i <- 0 to 2){
      val request = Request.get("http://localhost:8080/error")
      val f = client.call[Result](request).recover { case t =>
        Result("KO")
      }
      Await.result(f, Duration.Inf)
    }
    val request = Request.get("http://localhost:8080/test")
    val f1 = client.call[Result](request).recover { case t =>
        Result(t.getMessage)
    }
    val r1 = Await.result(f1, Duration.Inf)
    assert(r1 == Result("CircuitBreaker is opening."))

    Thread.sleep(1000)

    val f2 = client.call[Result](request)
    val r2 = Await.result(f2, Duration.Inf)
    assert(r2 == Result("OK"))
  }
}
