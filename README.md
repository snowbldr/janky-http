# Janky Http Server

An extremely minimal http server that barely implements enough of the protocol to be functional

Not recommend for use in general

### Start the server and handle requests

```kotlin
fun main() {
    JankyHttpServer { req ->
        when(req.path){
            "/" -> req.reply(Body.of("Hello world!"))
            else -> req.reply(status = 404, statusMessage = "NOT FOUND")
        }
    }
        .listen()
}
```

