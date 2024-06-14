package no.javabin.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import no.javabin.exception.DuplicateRegistrationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<DuplicateRegistrationException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message ?: "Conflict")
        }
    }
}