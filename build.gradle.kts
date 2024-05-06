val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val slf4j_version: String by project
val logstash_version: String by project
val flyway_version: String by project
val hikari_version: String by project
val postgres_version: String by project
val exposed_version: String by project
val h2_version: String by project
val kotlinx_datetime_version: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

group = "no.javabin"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")

    runtimeOnly("ch.qos.logback:logback-classic:$logback_version")
    runtimeOnly("org.slf4j:slf4j-api:$slf4j_version")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstash_version")

    // Auth
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth:jvm$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-metrics-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")


    //Ktor client dependencies
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("io.ktor:ktor-client-encoding:$ktor_version")

    // Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    // Logging
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Auth
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

    // Kotlin Query
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("com.github.seratch:kotliquery:1.9.0")

    // Datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_datetime_version")


    // Database
    // https://mvnrepository.com/artifact/org.flywaydb/flyway-core - database migrations
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-database-postgresql:$flyway_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP - connection pooling
    implementation("com.zaxxer:HikariCP:$hikari_version")
    // https://mvnrepository.com/artifact/org.postgresql/postgresql - database driver
    implementation("org.postgresql:postgresql:$postgres_version")
}
