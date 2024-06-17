package no.javabin.route

import no.javabin.service.WorkshopService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.javabin.config.CustomPrincipal

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
        get("/workshop/registration") {
            val user = call.principal<CustomPrincipal>()!!
            call.respond(workshopService.getWorkshopRegistrations(user.userId))
        }
        post("/workshop/{workshopId}/register") {
            val id = call.parameters["workshopId"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val user = call.principal<CustomPrincipal>()!!
            workshopService.registerWorkshop(id, user)
            call.respond(HttpStatusCode.OK, "Registered for workshop")
        }
        delete("/workshop/{workshopId}/unregister") {
            val id = call.parameters["workshopId"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val user = call.principal<CustomPrincipal>()!!
            workshopService.unregisterWorkshop(id, user)
            call.respond(HttpStatusCode.OK)
        }
    }
    authenticate("auth0-admin") {
        post("/update-workshop") {
            workshopService.workshopDatabaseUpdate()
            call.respond(HttpStatusCode.OK)
        }
    }


}

