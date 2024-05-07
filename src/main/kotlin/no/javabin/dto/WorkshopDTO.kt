package no.javabin.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class WorkshopDTO(
    val title: String,
    val description: String,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
    val active: Boolean,
    val speakers: List<SpeakerDTO>
)

