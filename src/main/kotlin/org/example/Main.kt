package org.example

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    val server = HttpServer.create(InetSocketAddress(8000), 0)
    server.createContext("/", MyHandler())
    server.executor = null // creates a default executor
    server.start()
}

class MyHandler : HttpHandler {
    override fun handle(t: HttpExchange) {
        val uri = t.getRequestURI()
        val response =
                "You requested: ${uri.path}\n" +
                        "Query: ${uri.query}\n" +
                        "Method: ${t.requestMethod}\n" +
                        "Headers: ${t.requestHeaders}\n" +
                        "Body: ${t.requestBody.bufferedReader().use { it.readText() }}\n"
        t.sendResponseHeaders(200, response.length.toLong())
        val os = t.responseBody
        os.write(response.toByteArray())
        os.close()
    }
}
