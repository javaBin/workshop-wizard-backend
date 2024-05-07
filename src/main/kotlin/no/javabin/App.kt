package no.javabin

import no.javabin.config.*
import com.inventy.plugins.DatabaseFactory
import io.ktor.server.application.*


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
    configureAuth()
    configureRouting()
}
