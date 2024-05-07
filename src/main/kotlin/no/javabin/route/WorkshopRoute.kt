package no.javabin.route

import no.javabin.service.WorkshopService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureWorkshopRoutes(
    workshopService: WorkshopService
) {
    routing {
        workshopRoute(workshopService)
    }
}

fun Routing.workshopRoute(
    workshopService: WorkshopService
) {
    authenticate("auth0-user") {
        get("/workshop") {
            call.respond(workshopService.getWorkshops())
        }
    }
    authenticate("auth0-admin") {
        post("/update-workshop") {
            workshopService.workshopDatabaseUpdate()
            call.respond(HttpStatusCode.OK)
        }
    }
}

