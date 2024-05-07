package no.javabin.config

import no.javabin.repository.AdminRepository
import no.javabin.repository.SpeakerRepository
import no.javabin.repository.UserRepository
import no.javabin.repository.WorkshopRepository
import no.javabin.route.*
import no.javabin.service.UserService
import no.javabin.service.WorkshopService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val adminRepository = AdminRepository()
    val workshopRepository = WorkshopRepository()
    val speakerRepository = SpeakerRepository()
    val workshopService = WorkshopService(environment.config, workshopRepository, speakerRepository)
    val userService = UserService(environment.config, workshopService, userRepository)

    configureAuth0Route(userRepository)
    configureUserRoutes(userService)
    configureWorkshopRoutes(workshopService)
    configureAdminRoutes(adminRepository)
    configureApiRoutes()

    routing {
        authenticate("auth0-user") {
            get("/auth") {
                call.respondText("Hello World!")
            }
        }

        get {
            call.respondText("Hello World!")
        }
    }
}
