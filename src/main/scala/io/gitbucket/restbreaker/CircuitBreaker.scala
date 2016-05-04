package io.gitbucket.restbreaker

import java.util.concurrent.{TimeoutException, Executors}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicLong}

import org.slf4j.LoggerFactory

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.reflect.ClassTag
import java.util.concurrent.TimeUnit._

class CircuitBreaker(client: ServiceClient,
                    enabled: Boolean = true,
                    callTimeout: Long = 5000,
                    maxFailures: Long = 5,
                    resetTimeout: Long = 30000) extends ServiceClient {

  protected val logger = LoggerFactory.getLogger(getClass)

  // Status of this CircuitBreaker
  protected val lastFailedTime = new AtomicLong(0)
  protected val failureCount = new AtomicInteger(0)
  protected val closed = new AtomicBoolean(true)

  def call[T](request: Request)(implicit ec: ExecutionContext, c: ClassTag[T]): Future[T] = {
    if(!enabled){
      call(request)
    } else {
      closed.get() match {
        case true => {
          val f = withTimeout(client.call(request))
          f.onFailure { case t =>
            val count = failureCount.incrementAndGet()
            if(count == maxFailures){
              closed.set(false)
              logger.info("CircuitBreaker is opened.")
            }
          }
          f
        }
        case false => {
          if(lastFailedTime.get() + resetTimeout < System.currentTimeMillis){
            logger.info("CircuitBreaker is opening, so returns the failure immediately.")
            Future.failed(new RuntimeException("CircuitBreaker is opening."))
          } else {
            val f = withTimeout(client.call(request))
            f.onSuccess { case response =>
              logger.info("CircuitBreaker is closed because the service is returned.")
              closed.set(true)
              failureCount.set(0)
              response
            }
            f
          }
        }
      }
    }
  }

  protected def withTimeout[T](f: Future[T]): Future[T] = {
    val scheduler = Executors.newScheduledThreadPool(1)
    val p = Promise[T]()
    p tryCompleteWith f
    val action = new Runnable {
      override def run(): Unit = {
        p tryFailure new TimeoutException("CircuitBreaker timed out ")
      }
    }
    scheduler.schedule(action, callTimeout, MILLISECONDS)
    p.future
  }

}
