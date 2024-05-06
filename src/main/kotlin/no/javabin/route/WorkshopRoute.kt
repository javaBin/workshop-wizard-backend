package no.javabin.route

import no.javabin.repository.Workshop
import no.javabin.repository.WorkshopRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureWorkshopRoutes(workshopRepository: WorkshopRepository) {
    routing {
        workshopRoute(workshopRepository)
    }
}

fun Routing.workshopRoute(workshopRepository: WorkshopRepository) {
    authenticate ("auth0-user"){
        get("/workshop") {
            call.respond(workshopRepository.list().map(Workshop::toDTO))
        }
    }

}

