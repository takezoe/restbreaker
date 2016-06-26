package restbreaker

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler

object TestServer {

  val server = new Server(8080)
  server.setHandler(new TestJettyHandler());

  def start(): Unit = {
    server.start()
  }

  def stop(): Unit = {
    server.stop()
  }

  class TestJettyHandler extends AbstractHandler {

    override def handle(target: String, baseRequest: org.eclipse.jetty.server.Request,
                        request: HttpServletRequest, response: HttpServletResponse): Unit = {
      target match {
        case "/test" =>
          response.setStatus(200)
          response.setContentType("application/json")
          response.getOutputStream.write(JsonUtils.serialize(Result("OK")).getBytes("UTF-8"))
          baseRequest.setHandled(true)
        case "/error" =>
          response.setStatus(500)
          response.setContentType("application/json")
          response.getOutputStream.write(JsonUtils.serialize(Result("KO")).getBytes("UTF-8"))
          baseRequest.setHandled(true)
        case "/timeout" =>
          Thread.sleep(1000)
          response.setStatus(200)
          response.setContentType("application/json")
          response.getOutputStream.write(JsonUtils.serialize(Result("OK")).getBytes("UTF-8"))
          baseRequest.setHandled(true)
      }
    }

  }

  case class Result(status: String)

}