package no.javabin.dto

import no.javabin.repository.WorkshopRegistrationState
import kotlinx.serialization.Serializable

@Serializable
data class AdminWorkshopRegistrationDTO(val firstName: String, val lastName: String, val email: String, val state: WorkshopRegistrationState)
