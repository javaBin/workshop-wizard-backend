package no.javabin.service

import no.javabin.config.defaultClient
import no.javabin.dto.WorkshopDTO
import no.javabin.dto.WorkshopListImportDTO
import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.config.*
import no.javabin.config.CustomPrincipal
import no.javabin.dto.WorkshopRegistrationDTO
import no.javabin.repository.*
import org.slf4j.LoggerFactory

class WorkshopService(
    private val config: ApplicationConfig,
    private val workshopRepository: WorkshopRepository,
    private val speakerRepository: SpeakerRepository,
    private val workshopRegistrationRepository: WorkshopRegistrationRepository
) {
    private val log = LoggerFactory.getLogger(WorkshopService::class.java)


    suspend fun getWorkshops(): List<WorkshopDTO> {
        return workshopRepository.list()
    }

    suspend fun getWorkshopRegistrations(userId: Int): List<WorkshopRegistrationDTO> {
        return workshopRegistrationRepository.list(userId)
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
        log.info("Registering user ${user.email} for workshop $workshopId")
        val workshop = workshopRepository.getById(workshopId)
        if (workshop == null) {
            log.warn("Workshop $workshopId not found")
            return
        }
        if (!workshop.active) {
            log.warn("Workshop $workshopId is not active")
            return
        }

        val registrations = workshopRegistrationRepository.getByWorkshop(workshopId)
        if (registrations.size >= workshop.capacity) {
            log.warn("Workshop $workshopId is full")
            return
        }

        val registration = workshopRegistrationRepository.getByWorkshopAndUser(workshopId, user.userId)
        if (registration != null) {
            when(registration.state){
                WorkshopRegistrationState.APPROVED -> {
                    log.warn("User ${user.email} is already registered for workshop $workshopId")
                    return
                }
                WorkshopRegistrationState.PENDING,
                WorkshopRegistrationState.WAITLIST,
                WorkshopRegistrationState.CANCELLED -> {
                    log.info("Re-registering user ${user.email} for workshop $workshopId")
                    workshopRegistrationRepository.updateState(registration.id, WorkshopRegistrationState.APPROVED)
                    return
                }
            }
        }

    }

    suspend fun unregisterWorkshop(workshopId: String, user: CustomPrincipal) {
        log.info("Unregistering user ${user.email} for workshop $workshopId")
        val workshop = workshopRepository.getById(workshopId)
        if (workshop == null) {
            log.warn("Workshop $workshopId not found")
            return
        }
        if (!workshop.active) {
            log.warn("Workshop $workshopId is not active")
            return
        }
        val registration = workshopRegistrationRepository.getByWorkshopAndUser(workshopId, user.userId)
        if (registration == null) {
            log.warn("User ${user.email} is not registered for workshop $workshopId")
            return
        } else {
            workshopRegistrationRepository.updateState(registration.id, WorkshopRegistrationState.CANCELLED)
        }
    }


}
