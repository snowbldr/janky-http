fun main() {
    JankyHttpServer { req ->
        when(req.path){
            "/" -> req.reply(Body.of("Hello world!"))
            else -> req.reply(status = 404, statusMessage = "NOT FOUND")
        }
    }
        .listen()
}