package no.javabin.service

import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.config.*
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock
import no.javabin.config.CustomPrincipal
import no.javabin.config.defaultClient
import no.javabin.dto.*
import no.javabin.repository.*
import no.javabin.repository.Speaker
import org.slf4j.LoggerFactory


class WorkshopService(
    private val config: ApplicationConfig,
    private val workshopRepository: WorkshopRepository,
    private val speakerRepository: SpeakerRepository,
    private val workshopRegistrationRepository: WorkshopRegistrationRepository,
    private val channel: Channel<WorkshopRegistrationMessage>,
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
        validateWorkshop(workshopId) ?: return
        val now = Clock.System.now()
        channel.send(
            WorkshopRegistrationMessage(
                workshopId = workshopId,
                messageType = WorkshopRegistrationMessageType.REGISTER,
                userId = user.userId,
                createdAt = now,
            )
        )
    }

    suspend fun unregisterWorkshop(workshopId: String, user: CustomPrincipal) {
        log.info("Unregistering user ${user.email} for workshop $workshopId")
        validateWorkshop(workshopId) ?: return
        getRegistrationOrNull(workshopId, user.userId) ?: return;

        sendEvent(workshopId, user.userId, WorkshopRegistrationMessageType.CANCEL)
    }

    private suspend fun sendEvent(
        workshopId: String,
        userId: Int,
        workshopRegistrationMessageType: WorkshopRegistrationMessageType
    ) {
        val now = Clock.System.now()
        log.debug("Sending workshop event, [{}, {}, {}]", userId, workshopId, workshopRegistrationMessageType)
        channel.send(
            WorkshopRegistrationMessage(
                workshopId = workshopId,
                messageType = workshopRegistrationMessageType,
                userId = userId,
                createdAt = now,
            )
        )
    }

    private suspend fun validateWorkshop(workshopId: String): Workshop? {
        val workshop = workshopRepository.getById(workshopId)
        if (workshop == null) {
            log.warn("Workshop $workshopId not found")
            return null
        }
        if (!workshop.active) {
            log.warn("Workshop $workshopId is not active")
            return null
        }
        return workshop
    }

    private suspend fun getRegistrationOrNull(workshopId: String, userId: Int): WorkshopRegistration? {
        val registration = workshopRegistrationRepository.getByWorkshopAndUser(workshopId, userId)
        if (registration == null) {
            log.warn("User $userId is not registered for workshop $workshopId")
            return null
        }
        return registration
    }
}
