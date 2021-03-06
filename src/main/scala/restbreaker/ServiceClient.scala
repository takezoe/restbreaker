package restbreaker

import java.nio.charset.StandardCharsets
import java.util

import org.asynchttpclient._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

trait ServiceClient {
  def call[T](request: Request)(implicit ec: ExecutionContext, c: ClassTag[T]): Future[T]
}

class AHCServiceClient(config: ServiceClientConfig = ServiceClientConfig()) extends ServiceClient {

  protected var client: AsyncHttpClient = new DefaultAsyncHttpClient()

  def call[T](request: Request)(implicit ec: ExecutionContext, c: ClassTag[T]): Future[T] = {
    val req = request.method match {
      case Get    => buildRequest(request, client.prepareGet(request.url))
      case Post   => buildRequest(request, client.preparePost(request.url))
      case Put    => buildRequest(request, client.preparePut(request.url))
      case Delete => buildRequest(request, client.prepareDelete(request.url))
      case Head   => buildRequest(request, client.prepareHead(request.url))
    }

    val promise = Promise[String]()
    req.execute(new AsyncResultHandler(promise))

    promise.future.map { body =>
      JsonUtils.deserialize[T](body)
    }
  }

  protected def buildRequest(request: Request, requestBuilder: BoundRequestBuilder): BoundRequestBuilder = {
    requestBuilder.setRequestTimeout(config.timeout)
    request.headers.foreach { case (key, value) =>
      requestBuilder.addHeader(key, value)
    }
    if(request.queryParams.nonEmpty){
      val queryParams = new util.HashMap[String, util.List[String]]()
      request.queryParams.foreach { case (key, values) =>
        queryParams.put(key, values.asJava)
      }
      requestBuilder.setQueryParams(queryParams)
    }
    request.body match {
      case Some(body) => {
        requestBuilder.setBody(JsonUtils.serialize(body)).setHeader("Content-Type", "application/json")
      }
      case None => {
        if(request.formParams.nonEmpty){
          val formParams = new util.HashMap[String, util.List[String]]()
          request.formParams.foreach { case (key, values) =>
            formParams.put(key, values.asJava)
          }
          requestBuilder.setFormParams(formParams)
        }
      }
    }

    requestBuilder
  }

  def shutdown(): Unit = {
    client.close()
  }
}

class AsyncResultHandler(promise: Promise[String]) extends AsyncCompletionHandler[Unit] {
  override def onCompleted(response: Response): Unit = {
    try {
      if (response.getStatusCode >= 200 && response.getStatusCode < 300) {
        promise.success(response.getResponseBody(StandardCharsets.UTF_8))
      } else {
        promise.failure(new HttpException(response))
      }
    } catch {
      case NonFatal(t) => promise.tryFailure(t)
    }
  }
  override def onThrowable(t: Throwable): Unit = {
    promise.failure(t)
  }
}

class HttpException(response: Response) extends RuntimeException

case class ServiceClientConfig(timeout: Int = 10000,
                               followRedirect: Boolean = false,
                               maxRedirects: Int = 0,
                               userAgent: Option[String] = None)
