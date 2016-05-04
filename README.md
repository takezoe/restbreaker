restbreaker
====

REST Breaker is an asynchronous HTTP client for REST API for Scala with the circuit breaker.

Basic usage
----

Basically, REST Breaker provides simple and intuitive API to call REST API.
Especially, it makes possible to handle JSON easily by encoding / decoding Scala object and JSON automatically.

```scala
val client = new AHCServiceClient()

// Assemble POST request which send JSON as request body
val request = Request.post("http://localhost:9000/api/blog").withBody(BlogPost(...))

// Send request
val f = client.call[BlogPostResult](request) recover { case e: Exception =>
  Result(false)
}

f.foreach { result: Result =>
  if(result.success){
    println("success")
  } else {
    println("failure")
  }
}
```

Of course, you can also set request headers, query parameters and form parameters.

```scala
val request = Request.post("http://localhost:9000/api/blog")
  .withHeader("Content-Type" -> "text/plain; charset=UTF-8")
  .withQueryParam("id" -> "123", "lang" -> "English")
  .withBody("Hello, World!")
```

Circuit Breaker
----

REST Breaker also provides the circuit breaker for the REST API calls.
To enable the circuit breaker, wrap `ServiceClient` by `CircuitBreaker` simply.

```scala
val client = new CircuitBreaker(new AHCServiceClient(), callTimeout = 1000, maxFailures = 5, resetTimeout = 30000)
```

If API call is failed `maxFailures` times, `CircuitBreaker` is opened and returns the failure immediately for subsequent calls.
After `resetTimeout` is elapsed, it tries to call the API and if it's success, `CircuitBreaker` is closed but otherwise, 
it returns the failure until `resetTimeout` is elapsed again.
