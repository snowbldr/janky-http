/**
 * A response body to be sent back on an open connection
 */
class Body(val bytes: ByteArray) {
    companion object {
        fun of(string: String?) = Body(string?.toByteArray() ?: ByteArray(0))
        fun empty() = Body(ByteArray(0))
    }
}
