package no.javabin.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.javabin.repository.AdminRepository
import no.javabin.repository.UserRepository
import no.javabin.route.*
import no.javabin.service.UserService
import no.javabin.service.WorkshopService

fun Application.configureRouting(
    userRepository: UserRepository,
    workshopService: WorkshopService,
    userService: UserService,
    adminRepository: AdminRepository,
) {
    configureAuth0Route(userRepository)
    configureUserRoutes(userService)
    configureWorkshopRoutes(workshopService)
    configureAdminRoutes(adminRepository)
    configureApiRoutes()

    routing {
        authenticate("auth0-user") {
            get("/auth") {
                call.respondText("Hello World User!")
            }
        }

        get {
            call.respondText("Hello World!")
        }
    }
}
