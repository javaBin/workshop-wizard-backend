package no.javabin.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import no.javabin.repository.WorkshopRegistrationState

@Serializable
class WorkshopRegistrationDTO (
    val workshopTitle: String,
    val workshopStartTime: Instant,
    val workshopEndTime: Instant,
    val state: WorkshopRegistrationState
)
