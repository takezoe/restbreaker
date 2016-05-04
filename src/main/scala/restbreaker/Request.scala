package restbreaker

case class Request(method: HttpMethod,
                   url: String,
                   headers: Map[String, String] = Map.empty,
                   queryParams: Map[String, Seq[String]] = Map.empty,
                   formParams: Map[String, Seq[String]] = Map.empty,
                   body: Option[AnyRef] = None){

  def withHeader(headers: (String, String)*): Request = copy(headers = this.headers ++ headers.toMap)

  def withQueryParam(queryParams: (String, Any)*) = {
    copy(queryParams = this.queryParams ++ queryParams.map { case (key, value) =>
      key -> (value match {
        case x: Seq[_] => x.map(_.toString)
        case _         => Seq(value.toString)
      })
    })
  }

  def withFormParam(formParams: (String, Any)*) = {
    copy(formParams = this.formParams ++ formParams.map { case (key, value) =>
      key -> (value match {
        case x: Seq[_] => x.map(_.toString)
        case _         => Seq(value.toString)
      })
    })
  }

  def withBody(body: AnyRef): Request = copy(body = Some(body))

}

object Request {
  def get(url: String): Request = Request(Get, url)
  def post(url: String): Request = Request(Post, url)
  def put(url: String): Request = Request(Put, url)
  def delete(url: String): Request = Request(Delete, url)
  def head(url: String): Request = Request(Head, url)
}

sealed trait HttpMethod
case object Get extends HttpMethod
case object Post extends HttpMethod
case object Put extends HttpMethod
case object Delete extends HttpMethod
case object Head extends HttpMethod