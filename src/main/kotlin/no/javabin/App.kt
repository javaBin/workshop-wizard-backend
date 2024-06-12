package no.javabin

import com.inventy.plugins.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import no.javabin.config.configureAuth
import no.javabin.config.configureRegistrationWorker
import no.javabin.config.configureRouting
import no.javabin.config.configureSerialization
import no.javabin.dto.WorkshopRegistrationMessage
import no.javabin.repository.UserRepository
import no.javabin.repository.WorkshopRegistrationRepository
import no.javabin.repository.WorkshopRepository
import no.javabin.service.RegistrationWorkerService


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureSerialization()
    DatabaseFactory(
        dbHost = environment.config.property("database.host").getString(),
        dbPort = environment.config.property("database.port").getString(),
        dbUser = environment.config.property("database.user").getString(),
        dbPassword = environment.config.property("database.password").getString(),
        databaseName = environment.config.property("database.databaseName").getString(),
        embedded = environment.config.property("database.embedded").getString().toBoolean(),
    ).init()
    val channel = Channel<WorkshopRegistrationMessage>(capacity = 20)
    val userRepository = UserRepository()
    val workshopRepository = WorkshopRepository()
    val workshopRegistrationRepository = WorkshopRegistrationRepository()
    val registrationWorkerService =
        RegistrationWorkerService(workshopRepository, workshopRegistrationRepository, channel)
    configureAuth(userRepository)
    configureRouting(userRepository, workshopRepository, workshopRegistrationRepository, channel)
    configureRegistrationWorker(registrationWorkerService)
}
