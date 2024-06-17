package no.javabin.config

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import no.javabin.repository.UserRepository
import java.util.concurrent.TimeUnit

class CustomPrincipal(payload: Payload, val userId: Int, val email: String): Principal, JWTPayloadHolder(payload)

fun Application.configureAuth(userRepository: UserRepository) {
    suspend fun validateCreds(credential: JWTCredential, checkAdmin: Boolean): CustomPrincipal? {
        val isRunningInTest = System.getProperty("RUNNING_IN_TEST") != null
        println("isRunningInTest: $isRunningInTest")
        if (isRunningInTest) {
            return CustomPrincipal(credential.payload, 1, "test@test.no")
        }

        val containsAudience = credential.payload.audience.contains(environment.config.property("auth0.audience").getString())
        val email = credential.payload.claims["user/email"]?.asString() ?: ""
        if (containsAudience && email.isNotEmpty()) {
            val user = userRepository.readByEmail(email) ?: throw Exception("User not found")
            if (checkAdmin) {
                if (!user.isAdmin) {
                    throw Exception("User is not admin")
                }
            }
            return CustomPrincipal(credential.payload, user.id, email)
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
            validate { credential -> validateCreds(credential, false) }
        }
        jwt("auth0-admin") {
            verifier(jwkProvider, issuer)
            validate { credential -> validateCreds(credential, true) }
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
