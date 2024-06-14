package no.javabin.service

import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.config.*
import no.javabin.config.CustomPrincipal
import no.javabin.config.defaultClient
import no.javabin.dto.WorkshopDTO
import no.javabin.dto.WorkshopListImportDTO
import no.javabin.dto.WorkshopRegistrationDTO
import no.javabin.exception.DuplicateRegistrationException
import no.javabin.repository.*
import org.slf4j.LoggerFactory


class WorkshopService(
    private val config: ApplicationConfig,
    private val workshopRepository: WorkshopRepository,
    private val speakerRepository: SpeakerRepository,
    private val workshopRegistrationRepository: WorkshopRegistrationRepository,
) {
    private val log = LoggerFactory.getLogger(WorkshopService::class.java)


    suspend fun getWorkshops(): List<WorkshopDTO> {
        return workshopRepository.getActiveWorkshops()
    }

    suspend fun getWorkshopRegistrations(userId: Int): List<WorkshopRegistrationDTO> {
        return workshopRegistrationRepository.getByUserId(userId)
    }

    suspend fun workshopDatabaseUpdate() {
        val client = HttpClient {
            defaultClient()
        }

        val response: WorkshopListImportDTO =
            client.get { url(config.property("workshopDatabase.workshopDataUrl").getString()) }.body()

        log.info(response.toString())

        val speakers = extractSpeakers(response)

        dbQuery {
            workshopRepository.update(response.sessions)
            speakerRepository.replace(speakers)
        }
    }

    private fun extractSpeakers(response: WorkshopListImportDTO): List<Speaker> {
        return response.sessions.flatMap { workshop ->
            workshop.speakers.withIndex().map { (index, speaker) ->
                Speaker(
                    name = speaker.name,
                    twitter = speaker.twitter ?: "",
                    bio = speaker.bio,
                    workshopId = workshop.id,
                    index = index,
                )
            }
        }
    }

    suspend fun registerWorkshop(workshopId: String, user: CustomPrincipal) {
        log.info("Registering user [${user.userId},${user.email}] for workshop $workshopId")
        val registration = workshopRegistrationRepository.getByWorkshopAndUser(workshopId, user.userId)

        if (registration?.status == WorkshopRegistrationStatus.APPROVED) throw DuplicateRegistrationException("Already registered")

        if (registration == null) {
            createNewRegistration(user.userId, workshopId)
        } else if (registration.status == WorkshopRegistrationStatus.CANCELLED) {
            workshopRegistrationRepository.updateStatus(
                registration.userId,
                registration.workshopId,
                WorkshopRegistrationStatus.WAIT_LIST
            )
        }
        updateWaitlistIfNeeded(workshopId)
    }

    suspend fun unregisterWorkshop(workshopId: String, user: CustomPrincipal) {
        log.info("Unregistering user ${user.email} for workshop $workshopId")
        val registration = workshopRegistrationRepository.getByWorkshopAndUser(workshopId, user.userId)
        if (registration == null) {
            log.warn("Registration not found for workshop ID: $workshopId and user ID: ${user.userId}")
            return
        }
        val updatedRows = workshopRegistrationRepository.updateStatus(
            registration.userId,
            registration.workshopId,
            WorkshopRegistrationStatus.CANCELLED
        )

        if (updatedRows == 0) {
            log.debug("No rows was updated")
        }
        log.info("Registration cancelled for user: ${registration.userId}, workshop: ${registration.workshopId}")

        updateWaitlistIfNeeded(workshopId)
    }

    private suspend fun createNewRegistration(
        userId: Int,
        workshopId: String,
    ) {
        workshopRegistrationRepository.create(
            workshopId, userId, WorkshopRegistrationStatus.WAIT_LIST,
        )
    }

    private suspend fun updateWaitlistIfNeeded(workshopId: String) {
        val capacity = workshopRepository.getById(workshopId)?.capacity ?: return
        val registrations = workshopRegistrationRepository.getByWorkshop(workshopId)
        val waitlistRegistrations = registrations.filter { it.status == WorkshopRegistrationStatus.WAIT_LIST }
        val approvedCount = registrations.count { it.status == WorkshopRegistrationStatus.APPROVED }
        val freeSpaces = capacity - approvedCount

        if (capacity > approvedCount && waitlistRegistrations.isNotEmpty()) {

            log.info("Workshop [$workshopId] has [$freeSpaces] free spaces and [$waitlistRegistrations] in waitlist.\nPromoting waitlisters now.")
            promoteWaitlistedRegistrations(waitlistRegistrations, freeSpaces)

            log.info("Updated waitlist for workshop: $workshopId")
        } else {
            log.info("Skipping waitlist promotion for workshop [$workshopId]. [Free spaces: $freeSpaces, waitlist size: $waitlistRegistrations] free spaces. Promoting waitlisters now.")
        }
    }

    private suspend fun promoteWaitlistedRegistrations(
        waitlistRegistrations: List<WorkshopRegistration>,
        freeSpaces: Int
    ) {
        waitlistRegistrations
            .take(freeSpaces)
            .forEach { registration ->
                workshopRegistrationRepository.updateStatus(
                    registration.userId,
                    registration.workshopId,
                    WorkshopRegistrationStatus.APPROVED
                )
            }
    }
}
