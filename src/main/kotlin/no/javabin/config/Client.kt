package no.javabin.config

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.serialization.kotlinx.json.*
import io.netty.handler.codec.compression.StandardCompressionOptions.deflate
import io.netty.handler.codec.compression.StandardCompressionOptions.gzip
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.defaultClient() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(ContentEncoding){
        gzip()
        deflate()
    }

    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
    // Add all the common configuration here.
}
