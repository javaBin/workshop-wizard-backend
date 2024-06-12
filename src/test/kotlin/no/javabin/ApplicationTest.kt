package no.javabin

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.javabin.config.configureRouting
import no.javabin.repository.*
import no.javabin.service.UserService
import no.javabin.service.WorkshopService
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        System.setProperty("RUNNING_IN_TEST", "true")
        application {

            val userRepository = UserRepository()
            val workshopRepository = WorkshopRepository()
            val workshopRegistrationRepository = WorkshopRegistrationState.WorkshopRegistrationRepository()
            val adminRepository = AdminRepository()
            val speakerRepository = SpeakerRepository()
            val workshopService = WorkshopService(
                environment.config,
                workshopRepository,
                speakerRepository,
                workshopRegistrationRepository
            )
            val userService = UserService(environment.config, workshopService, userRepository)

            configureRouting(userRepository, workshopService, userService, adminRepository)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
