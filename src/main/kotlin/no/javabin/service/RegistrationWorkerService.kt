package no.javabin.service

import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import no.javabin.dto.WorkshopRegistrationMessage
import no.javabin.dto.WorkshopRegistrationMessageType
import no.javabin.repository.WorkshopRegistration
import no.javabin.repository.WorkshopRegistrationRepository
import no.javabin.repository.WorkshopRegistrationStatus
import no.javabin.repository.WorkshopRepository
import org.slf4j.LoggerFactory

class RegistrationWorkerService(
    private val workshopRepository: WorkshopRepository,
    private val workshopRegistrationRepository: WorkshopRegistrationRepository,
    private val channel: Channel<WorkshopRegistrationMessage>,
) {
    private val log = LoggerFactory.getLogger(WorkshopService::class.java)

    suspend fun handleMessages() {
        log.debug("Waiting for event, [process id: {}]", Thread.currentThread().threadId())
        val workshopRegistrationMessage = channel.receive()
        log.debug(
            "Event received [{}, user Id: {}, workshop id:{}]",
            workshopRegistrationMessage.messageType,
            workshopRegistrationMessage.userId,
            workshopRegistrationMessage.workshopId,
        )

        if (workshopRegistrationMessage.messageType == WorkshopRegistrationMessageType.REGISTER) {
            createRegistration(workshopRegistrationMessage)
        } else if (workshopRegistrationMessage.messageType == WorkshopRegistrationMessageType.CANCEL) {
            createCancelRegistration(workshopRegistrationMessage)
        }
    }

    private suspend fun createRegistration(workshopRegistrationMessage: WorkshopRegistrationMessage) {
        val workshopId = workshopRegistrationMessage.workshopId
        val userId = workshopRegistrationMessage.userId

        val capacity = workshopRepository.getById(workshopId)?.capacity ?: return
        val registrations = workshopRegistrationRepository.getByWorkshop(workshopId)

        if (isUserAlreadyApproved(registrations, userId)) return
        if (!isUserAlreadyInWaitlist(registrations, userId)){
            handleRegistration(workshopId, userId, capacity, registrations, workshopRegistrationMessage.createdAt)
        }
        updateWaitlistIfNeeded(workshopId)

    }

    private fun isUserAlreadyApproved(registrations: List<WorkshopRegistration>, userId: Int): Boolean {
        return registrations.any { it.userId == userId && it.status == WorkshopRegistrationStatus.APPROVED }
    }

    private fun isUserAlreadyInWaitlist(registrations: List<WorkshopRegistration>, userId: Int): Boolean {
        return registrations.any { it.userId == userId && it.status == WorkshopRegistrationStatus.WAITLIST }
    }

    private suspend fun handleRegistration(
        workshopId: String,
        userId: Int,
        capacity: Int,
        registrations: List<WorkshopRegistration>,
        creationTime: Instant
    ) {
        val approvedCount = registrations.count { it.status == WorkshopRegistrationStatus.APPROVED }
        val waitlistCount = registrations.count { it.status == WorkshopRegistrationStatus.WAITLIST }



        val status =
            if (capacity > approvedCount) WorkshopRegistrationStatus.APPROVED else WorkshopRegistrationStatus.WAITLIST

        val userRegistration = registrations.find { it.userId == userId }

        if (userRegistration != null) {
            updateExistingRegistration(userRegistration, status)
        } else {
            createNewRegistration(userId, workshopId, creationTime, status)
        }
    }

    private suspend fun updateExistingRegistration(
        registration: WorkshopRegistration,
        status: WorkshopRegistrationStatus
    ) {
        workshopRegistrationRepository.updateStatus(registration.userId, registration.workshopId, status)
    }

    private suspend fun createNewRegistration(
        userId: Int,
        workshopId: String,
        createdAt: Instant,
        status: WorkshopRegistrationStatus
    ) {
        workshopRegistrationRepository.create(
            WorkshopRegistration(
                id = null,
                userId = userId,
                workshopId = workshopId,
                createdAt = createdAt,
                updatedAt = createdAt,
                status = status,
            )
        )
    }

    private suspend fun createCancelRegistration(workshopRegistrationMessage: WorkshopRegistrationMessage) {
        val userId = workshopRegistrationMessage.userId
        val workshopId = workshopRegistrationMessage.workshopId

        val registration = workshopRegistrationRepository.getByWorkshopAndUser(workshopId, userId) ?: return
        updateExistingRegistration(registration, WorkshopRegistrationStatus.CANCELLED)
        log.info("Registration cancelled for user: ${registration.userId}, workshop: ${registration.workshopId}")

        updateWaitlistIfNeeded(workshopId)
    }

    private suspend fun updateWaitlistIfNeeded(workshopId: String) {
        val capacity = workshopRepository.getById(workshopId)?.capacity ?: return
        val registrations = workshopRegistrationRepository.getByWorkshop(workshopId)

        val approvedCount = registrations.count { it.status == WorkshopRegistrationStatus.APPROVED }

        if (capacity > approvedCount) {
            promoteWaitlistedRegistrations(registrations, capacity - approvedCount)
            log.info("Updated waitlist for workshop: $workshopId")
        }
    }

    private suspend fun promoteWaitlistedRegistrations(registrations: List<WorkshopRegistration>, freeSpaces: Int) {
        registrations.filter { it.status == WorkshopRegistrationStatus.WAITLIST }
            .take(freeSpaces)
            .forEach { registration ->
                workshopRegistrationRepository.createOrUpdate(
                    WorkshopRegistration(
                        id = registration.id,
                        userId = registration.userId,
                        workshopId = registration.workshopId,
                        createdAt = registration.createdAt,
                        updatedAt = Clock.System.now(),
                        status = WorkshopRegistrationStatus.APPROVED
                    )
                )
            }
    }
}