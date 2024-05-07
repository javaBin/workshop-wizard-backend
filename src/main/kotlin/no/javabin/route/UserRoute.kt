package no.javabin.route

import no.javabin.config.CustomPrincipal
import no.javabin.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureUserRoutes(userService: UserService) {
    routing {
        userRoutes(userService)
    }
}

fun Routing.userRoutes(userService: UserService) {
    authenticate("auth0-user") {
        get("/user/details") {
            // Should be based on the logged in user
            val email = call.authentication.principal<CustomPrincipal>()?.email!!
            val userInfo = userService.readByEmail(email)
            if (userInfo != null) {
                call.respond(userInfo.toDTO())
            }
            call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }


//    post("/user/workshop/{workshopId}") {
//        try {
//            val userId = call.authentication.principal<CustomPrincipal>()?.userId!!
//            userRepository.addWorkshopRegistrations(userId, call.parameters["workshopId"]!!.toInt())
//            call.respondText("Workshop added")
//        } catch (e: Exception) {
//            call.respondText("Workshop not found", status = io.ktor.http.HttpStatusCode.NotFound)
//        }
//    }
//
//    put("/user/workshop/{workshopId}/:cancel") {
//        val userId = call.authentication.principal<CustomPrincipal>()?.userId!!
//        userRepository.cancelWorkshopRegistration(userId, call.parameters["workshopId"]!!.toInt())
//        call.respondText("Workshop cancelled")
//    }
}
