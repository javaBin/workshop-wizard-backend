package no.javabin

import com.inventy.plugins.DatabaseFactory
import io.ktor.server.application.*
import no.javabin.config.configureAuth
import no.javabin.config.configureRouting
import no.javabin.config.configureSerialization
import no.javabin.repository.*
import no.javabin.service.UserService
import no.javabin.service.WorkshopService


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
    val userRepository = UserRepository()
    val workshopRepository = WorkshopRepository()
    val workshopRegistrationRepository = WorkshopRegistrationRepository()
    val adminRepository = AdminRepository()
    val speakerRepository = SpeakerRepository()
    val workshopService = WorkshopService(
        environment.config,
        workshopRepository,
        speakerRepository,
        workshopRegistrationRepository
    )
    val userService = UserService(environment.config, workshopService, userRepository)

    configureAuth(userRepository)
    configureRouting(userRepository, workshopService, userService, adminRepository)
}
