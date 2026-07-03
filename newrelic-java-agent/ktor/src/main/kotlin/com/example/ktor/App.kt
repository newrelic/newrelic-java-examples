package com.example.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO as ClientCIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data class GreetingResponse(val message: String, val timestamp: Long)

fun main() {
    val cioClient = HttpClient(ClientCIO) {
        install(ClientContentNegotiation) { json() }
        engine {
            requestTimeout = 10_000
        }
    }

    embeddedServer(Netty, port = 8080) {
        install(ServerContentNegotiation) { json() }

        routing {
            get("/hello") {
                call.respondText("Hello, World!")
            }

            get("/greet/{name}") {
                val name = call.parameters["name"] ?: "stranger"
                call.respondText("Hello, $name!")
            }

            post("/echo") {
                val body = call.receiveText()
                call.respondText(body)
            }

            get("/json") {
                call.respond(GreetingResponse("Hello from Ktor / Netty", System.currentTimeMillis()))
            }

            get("/slow") {
                delay(300)
                call.respondText("Slow response after 300ms delay")
            }

            get("/error") {
                error("Intentional error for NR error-collector testing")
            }

            get("/chain") {
                val response = cioClient.get("http://localhost:8080/hello")
                call.respondText("Chain: ${response.bodyAsText()}")
            }

            get("/async-deep") {
                delay(100)
                val mid = cioClient.get("http://localhost:8080/json").bodyAsText()
                delay(100)
                call.respondText("Async-deep mid=$mid")
            }
        }
    }.start(wait = true)
}
