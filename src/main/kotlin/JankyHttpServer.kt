import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

/**
 * A really janky http server that barely implements enough of the protocol to function
 */
class JankyHttpServer(private val requestHandler: (JankyHttpRequest) -> Unit) {
    private lateinit var server: ServerSocket

    fun listen(port: Int = 22420) {
        server = ServerSocket(port)
        println("Server started on port 22420")
        runBlocking {
            while (true) {
                server.accept().let {
                    coroutineScope { launch { handleConnection(it) } }
                }
            }
        }
    }

    private fun handleConnection(socket: Socket) {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        while (!socket.isClosed) {
            var line: String = reader.readLine()
            val reqInfo = line.split(" ")
            val headers = mutableMapOf<String, String>()
            while (line != "" && !socket.isClosed) {
                headers.putAll(
                    reader.readLine().also { line = it }
                        .split(": ")
                        .zipWithNext()
                        .map { it.copy(first = it.first.lowercase()) }
                )
            }
            if (socket.isClosed) return
            val body = if (headers.containsKey("content-length")) {
                Body(socket.getInputStream().readNBytes(headers["content-length"]!!.toInt()))
            } else {
                Body.empty()
            }
            val req = JankyHttpRequest(
                reqInfo[0],
                reqInfo[1],
                headers,
                body,
                BufferedOutputStream(socket.getOutputStream())
            )
            try {
                requestHandler(req)
            } catch (e: Exception) {
                req.reply(body = Body.of(e.message), status = 500, statusMessage = "INTERNAL SERVER ERROR")
                e.printStackTrace()
            }
        }
    }
}

