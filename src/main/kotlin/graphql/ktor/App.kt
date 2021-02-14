package graphql.ktor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class User(val id: Int?, val name: String, val age: Int)

object Users : Table() {
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", 255)
    val age: Column<Int> = integer("age")

    override val primaryKey = PrimaryKey(id, name="pk_user_id")

    fun toUser(row: ResultRow): User =
            User(
                    id = row[id],
                    name = row[name],
                    age = row[age]
            )
}

fun Application.app() {
    install(ContentNegotiation) {
        jackson {
            // Jackson config goes here
        }
    }

    Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
    transaction {
        SchemaUtils.create(Users)

        Users.insert {
            it[name] = "John"
            it[age] = 36
        }

        Users.insert {
            it[name] = "Jack"
            it[age] = 24
        }
    }

    install(Routing) {
        route("user") {
            get("/") {
                val users = transaction {
                    Users.selectAll().map { Users.toUser(it) }
                }
                call.respond(users)
            }

            get("/{id}") {
                val id = call.parameters["id"]!!.toInt()
                val users = transaction {
                    Users.select { Users.id eq id }.map { Users.toUser(it) }
                }
                call.respond(users)
            }

            post("/") {
                val user = call.receive<User>()
                transaction {
                    Users.insert {
                        it[name] = user.name
                        it[age] = user.age
                    }
                }
                call.respond(user)
            }
        }

    }

    routing {
        get("/") {
            call.respond(User(id = 1, name = "Bacano", age = 5))
        }

        post("/") {
            val user = call.receive<User>()
            call.respond(user)
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
