package restbreaker

import java.util.concurrent.TimeoutException

import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import TestServer._

class ServiceClientSpec extends FunSuite with BeforeAndAfter {

  before {
    TestServer.start()
  }

  after {
    TestServer.stop()
  }

  test("success"){
    val client = new AHCServiceClient()
    val request = Request.get("http://localhost:8080/test")
    val f = client.call[Result](request)
    val r = Await.result(f, Duration.Inf)
    assert(r == Result("OK"))
  }

  test("error"){
    val client = new AHCServiceClient()
    val request = Request.get("http://localhost:8080/error")
    val f = client.call[Result](request).recover { case t: HttpException =>
      Result("KO")
    }
    val r = Await.result(f, Duration.Inf)
    assert(r == Result("KO"))
  }

  test("timeout"){
    val client = new AHCServiceClient(ServiceClientConfig(timeout = 500))
    val request = Request.get("http://localhost:8080/timeout")
    val f = client.call[Result](request).recover { case t: TimeoutException =>
        Result("KO")
    }
    val r = Await.result(f, Duration.Inf)
    assert(r == Result("KO"))
  }
}

