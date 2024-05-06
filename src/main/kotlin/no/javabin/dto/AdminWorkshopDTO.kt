package no.javabin.dto

import no.javabin.dto.AdminWorkshopRegistrationDTO
import kotlinx.serialization.Serializable

@Serializable
data class AdminWorkshopDTO(
    val title: String,
    val teacherName: String,
    val registrations: List<AdminWorkshopRegistrationDTO>,
)
