package no.javabin

import no.javabin.config.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statusment.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        System.setProperty("RUNNING_IN_TEST", "true")
        application {
            configureRouting(userRepository)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
