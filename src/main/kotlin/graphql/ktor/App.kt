package graphql.ktor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

data class User(val name: String, val size: Long)

fun Application.app() {
    routing {
        get("/") {
            call.respond(User(name = "Bacano", size = 5))
        }

        post("/") {
            val user = call.receive<User>()
            call.respond(user)
        }
    }

    install(ContentNegotiation) {
        jackson {
           // Jackson config goes here
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(
            Netty,
            watchPaths = listOf("graphql.ktor"),
            module = Application::app,
            port = 8080
    ).start(wait = true)
}
