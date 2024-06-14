package no.javabin.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import no.javabin.repository.WorkshopRegistrationStatus

@Serializable
class WorkshopRegistrationDTO (
    val workshopTitle: String,
    val workshopStartTime: Instant,
    val workshopEndTime: Instant,
    val status: WorkshopRegistrationStatus
)
