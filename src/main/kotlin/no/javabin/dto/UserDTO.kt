package no.javabin.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val firstName: String,
    val lastName: String,
    val email: String,
    val imageUrl: String,
    val isAdmin: Boolean,
) : DTO

