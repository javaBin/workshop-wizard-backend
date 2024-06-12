package no.javabin.dto

import kotlinx.datetime.Instant

class WorkshopRegistrationMessage(
    val userId: Int,
    val workshopId: String,
    val createdAt: Instant,
    var messageType: WorkshopRegistrationMessageType,
);

enum class WorkshopRegistrationMessageType {
    REGISTER, CANCEL
}