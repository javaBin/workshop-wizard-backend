package no.javabin

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.javabin.config.configureRouting
import no.javabin.repository.UserRepository
import org.junit.Test
import kotlin.test.assertEquals

class WorkshopRegistrationTest {

    // Test running multiple registrations for the same workshop
    @Test
    fun testMultipleRegistrations()  = testApplication {
        System.setProperty("RUNNING_IN_TEST", "true")
        application {
            val userRepository = UserRepository()
            configureRouting(userRepository)
        }
        client.get("/workshop").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[]", bodyAsText())
        }
        client.post("/update-workshop").apply {
            assertEquals(HttpStatusCode.OK, status)

        }
    }
}
