REST Breaker
====

REST Breaker is an asynchronous REST client with the circuit breaker for Scala.

Basic usage
----

Basically, REST Breaker provides simple and intuitive API to call JSON based REST API.
It makes possible to handle JSON easily by encoding / decoding Scala object and JSON automatically.

```scala
import restbreaker._

val client = new AHCServiceClient()

// Assemble POST request which send JSON as request body
val request = Request.post("http://localhost:9000/api/blog").withBody(BlogPost(...))

// Send request
val f = client.call[Result](request) recover { case e: Exception =>
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
