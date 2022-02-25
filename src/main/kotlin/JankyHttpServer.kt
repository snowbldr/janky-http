import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.Arrays

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
                Body(readNBytes(socket.getInputStream(), headers["content-length"]!!.toInt()))
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

/**
 * Polyfill of java11 InputStream.readNBytes to allow building by java8 because that's what jitpack uses
 */
const val DEFAULT_BUFFER_SIZE = 8192
const val MAX_BUFFER_SIZE = Int.MAX_VALUE - 8
fun readNBytes(input: InputStream, len: Int): ByteArray {
    require(len >= 0) { "len < 0" }
    var bufs: MutableList<ByteArray>? = null
    var result: ByteArray? = null
    var total = 0
    var remaining = len
    var n: Int
    do {
        var buf = ByteArray(Math.min(remaining, DEFAULT_BUFFER_SIZE))
        var nread = 0

        // read to EOF which may read more or less than buffer size
        while (input.read(
                buf, nread,
                Math.min(buf.size - nread, remaining)
            ).also { n = it } > 0
        ) {
            nread += n
            remaining -= n
        }
        if (nread > 0) {
            if (MAX_BUFFER_SIZE - total < nread) {
                throw OutOfMemoryError("Required array size too large")
            }
            if (nread < buf.size) {
                buf = Arrays.copyOfRange(buf, 0, nread)
            }
            total += nread
            if (result == null) {
                result = buf
            } else {
                if (bufs == null) {
                    bufs = ArrayList()
                    bufs.add(result)
                }
                bufs.add(buf)
            }
        }
        // if the last call to read returned -1 or the number of bytes
        // requested have been read then break
    } while (n >= 0 && remaining > 0)
    if (bufs == null) {
        if (result == null) {
            return ByteArray(0)
        }
        return if (result.size == total) result else Arrays.copyOf(result, total)
    }
    result = ByteArray(total)
    var offset = 0
    remaining = total
    for (b in bufs) {
        val count = Math.min(b.size, remaining)
        System.arraycopy(b, 0, result, offset, count)
        offset += count
        remaining -= count
    }
    return result
}
