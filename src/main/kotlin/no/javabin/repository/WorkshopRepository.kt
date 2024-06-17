package no.javabin.repository

import no.javabin.dto.SpeakerDTO
import no.javabin.dto.WorkshopDTO
import no.javabin.dto.WorkshopImport
import no.javabin.util.TimeUtil
import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import kotlinx.datetime.Instant
import no.javabin.repository.WorkshopRepository.WorkshopTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

data class Workshop(
    val id: String,
    val title: String,
    val description: String,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
    val active: Boolean
)

class WorkshopRepository {

    companion object {
        private const val WORKSHOP_CAPACITY = 30
    }

    internal object WorkshopTable : Table() {

        val id = varchar("id", 64)
        val title = varchar("title", 256)
        val description = varchar("description", 2048)
        val startTime = timestamp("start_time")
        val endTime = timestamp("end_time")
        val capacity = integer("capacity")
        val active = bool("active")

        override val primaryKey = PrimaryKey(arrayOf(id), "id")

        fun toModel(it: ResultRow) = Workshop(
            id = it[id],
            title = it[title],
            description = it[description],
            startTime = it[startTime],
            endTime = it[endTime],
            capacity = it[capacity],
            active = it[active],
        )

        fun toDTO(it: ResultRow) = WorkshopDTO(
            id = it[id],
            title = it[title],
            description = it[description],
            startTime = TimeUtil.toGmtPlus2(it[startTime]),
            endTime = TimeUtil.toGmtPlus2(it[endTime]),
            capacity = it[capacity],
            active = it[active],
            speakers = listOf(
                SpeakerDTO(
                    name = it[SpeakerRepository.SpeakerTable.name],
                    bio = it[SpeakerRepository.SpeakerTable.bio],
                    twitter = it[SpeakerRepository.SpeakerTable.twitter]
                )
            )
        )
    }

    suspend fun getActiveWorkshops(): List<WorkshopDTO> = dbQuery {
        (WorkshopTable innerJoin SpeakerRepository.SpeakerTable)
            .selectAll().where { WorkshopTable.active eq true }
            .map(WorkshopTable::toDTO)
            .groupBy { it.title }
            .map { (_, workshops) ->
                workshops.first().copy(
                    speakers = workshops.flatMap { it.speakers }
                )
            }
    }

    suspend fun getById(id: String): Workshop? = dbQuery {
        WorkshopTable.selectAll().where { WorkshopTable.id eq id }
            .map(WorkshopTable::toModel)
            .singleOrNull()
    }

    private suspend fun getByIdsNotInList(idLIst: List<String>): List<Workshop> = dbQuery {
        WorkshopTable.selectAll().where(WorkshopTable.id notInList idLIst)
            .map(WorkshopTable::toModel)
    }


    private suspend fun upsertActive(workshops: List<WorkshopImport>) = dbQuery {
        WorkshopTable.batchUpsert(workshops) { workshop ->
            this[WorkshopTable.id] = workshop.id
            this[WorkshopTable.title] = workshop.title
            this[WorkshopTable.description] =
                workshop.abstract.take(250) + if (workshop.abstract.length > 250) " ..." else ""
            this[WorkshopTable.startTime] = Instant.parse(workshop.startTimeZulu)
            this[WorkshopTable.endTime] = Instant.parse(workshop.endTimeZulu)
            this[WorkshopTable.capacity] = WORKSHOP_CAPACITY
            this[WorkshopTable.active] = true
        }
    }

    private suspend fun upsert(workshops: List<Workshop>) = dbQuery {
        WorkshopTable.batchUpsert(workshops) { workshop ->
            this[WorkshopTable.id] = workshop.id
            this[WorkshopTable.title] = workshop.title
            this[WorkshopTable.description] = workshop.description
            this[WorkshopTable.startTime] = workshop.startTime
            this[WorkshopTable.endTime] = workshop.endTime
            this[WorkshopTable.capacity] = WORKSHOP_CAPACITY
            this[WorkshopTable.active] = workshop.active
        }
    }

    private suspend fun setWorkshopsToDisabled(activeWorkshops: List<WorkshopImport>) {
        val activeWorkshopsIds = activeWorkshops.map { it.id }
        val allDisabledList = getByIdsNotInList(activeWorkshopsIds)
            .map { it.copy(active = false) }
        upsert(allDisabledList)
    }

    suspend fun update(workshops: List<WorkshopImport>) {
        dbQuery {
            setWorkshopsToDisabled(workshops)
            upsertActive(workshops)
        }
    }
}
