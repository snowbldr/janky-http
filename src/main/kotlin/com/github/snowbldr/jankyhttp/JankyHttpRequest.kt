package com.github.snowbldr.jankyhttp

import java.io.BufferedOutputStream

/**
 * An incoming http request
 */
class JankyHttpRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val body: Body,
    private val output: BufferedOutputStream,
) {
    /**
     * Send a response back to the caller, optionally with a body
     */
    fun reply(body: Body? = null, status: Int = 200, statusMessage: String = "OK") {
        output.write("HTTP/1.1 $status $statusMessage\r\nContent-Length: ${body?.bytes?.size ?: 0}\r\n\r\n".toByteArray())
        body?.let { output.write(it.bytes) }
        output.flush()
    }
}
