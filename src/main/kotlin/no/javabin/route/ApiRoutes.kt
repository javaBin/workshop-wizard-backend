package no.javabin.route

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureApiRoutes() {
    routing {
        healthz()
    }
}

fun Routing.healthz() {
    get("readiness") {
        call.respondText("READY!")
    }

    get("liveness") {
        call.respondText("ALIVE!")
    }
}
