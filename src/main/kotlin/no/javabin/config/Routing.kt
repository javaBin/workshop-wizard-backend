package no.javabin.config

import no.javabin.repository.AdminRepository
import no.javabin.repository.UserRepository
import no.javabin.repository.WorkshopRepository
import no.javabin.route.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val adminRepository = AdminRepository()
    val workshopRepository = WorkshopRepository()

    configureAuth0Route(userRepository)
    configureUserRoutes(userRepository)
    configureWorkshopRoutes(workshopRepository)
    configureAdminRoutes(adminRepository)
    configureApiRoutes()

    routing {
        authenticate ("auth0-user") {
            get("/auth") {
                call.respondText("Hello World!")
            }
        }

        get {
            call.respondText("Hello World!")
        }
    }
}
