package no.javabin.config

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.concurrent.TimeUnit

class CustomPrincipal(payload: Payload, val userId: Int, val email: String): Principal, JWTPayloadHolder(payload)

fun Application.configureAuth() {
    fun validateCreds(credential: JWTCredential): CustomPrincipal? {
        val containsAudience = credential.payload.audience.contains(environment.config.property("auth0.audience").getString())
        val email = credential.payload.claims["user/email"]?.asString() ?: ""
        if (containsAudience) {
            return CustomPrincipal(credential.payload, 1, email)
        }

        return null
    }

    val issuer = environment.config.property("auth0.issuer").getString()
    val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt("auth0-user") {
            verifier(jwkProvider, issuer)
            validate { credential -> validateCreds(credential) }
        }
        jwt("auth0-admin") {
            verifier(jwkProvider, issuer)
            validate { credential -> validateCreds(credential) }
        }
        basic("basic-auth0") {
            realm = "Used by auth0 for creating users"
            validate { credentials ->
                if (credentials.name == "auth0" && credentials.password == "password") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}
