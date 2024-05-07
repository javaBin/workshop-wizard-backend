package no.javabin.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpeakerDTO(
    val name: String,
    val bio: String,
    val twitter: String,
)
