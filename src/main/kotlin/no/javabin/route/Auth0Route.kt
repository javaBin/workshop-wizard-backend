package no.javabin.route

import no.javabin.dto.UserDTO
import no.javabin.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAuth0Route(userRepository: UserRepository) {
    routing {
        auth0Route(userRepository)
    }
}

fun Route.auth0Route(userRepository: UserRepository) {
    authenticate ("basic-auth0") {
        get("/auth0{email}") {
            val email = call.parameters["email"]
            if (email == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing email parameter: requiredParam")
                return@get
            }
            val userDTO = userRepository.readByEmail(email)
            if (userDTO != null) {
                call.respond(true)
            } else {
                call.respond(false)
            }
        }
        post("/auth0") {
            val userDTO = call.receive<UserDTO>()
            val user = userRepository.readByEmail(userDTO.email)
            if (user?.id != null) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
                return@post
            } else {
                val create = userRepository.create(userDTO)
                application.log.info("User created with id: $create")
                call.respond(HttpStatusCode.Created)
                return@post
            }
        }
    }
}
