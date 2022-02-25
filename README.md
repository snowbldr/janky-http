# Janky Http Server

An extremely minimal http server that barely implements enough of the protocol to be functional

Not recommend for use in general, but would do an ok job as an alive/ready check

### Add Dependency


### Run
JankyHttpServer takes a function to execute when a request comes in and starts listening on port 22420 as soon as you create it

```kotlin
import com.github.snowbldr.jankyhttp.Body
import com.github.snowbldr.jankyhttp.JankyHttpServer

fun main() {
    JankyHttpServer { req ->
        when(req.path){
            "/" -> req.reply(Body.of("Hello world!"))
            else -> req.reply(status = 404, statusMessage = "NOT FOUND")
        }
    }
}
```

You can specify the port as the first parameter to JankyHttpServer

```kotlin
fun main() {
    JankyHttpServer(8080) { req -> req.reply(Body.of("Look, I'm the internet!")) }
}
```