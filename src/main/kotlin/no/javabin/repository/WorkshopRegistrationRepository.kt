package no.javabin.repository

import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import no.javabin.dto.WorkshopRegistrationDTO
import no.javabin.repository.WorkshopRegistrationRepository.WorkshopRegistrationTable
import no.javabin.util.TimeUtil
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


enum class WorkshopRegistrationState {
    PENDING, WAIT_LIST, APPROVED, CANCELLED,
}

class WorkshopRegistration(
    override val id: Int,
    val userId: Int,
    val workshopId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    var state: WorkshopRegistrationState = WorkshopRegistrationState.PENDING,
) : Model

class WorkshopRegistrationRepository {
    private object WorkshopRegistrationTable : IntIdTable("workshop_registration") {
        val userId = reference("user_id", UserRepository.UserTable.id)
        val workshopId = reference("workshop_id", WorkshopRepository.WorkshopTable.id)
        val state = enumerationByName<WorkshopRegistrationState>("state", 64)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")

        fun toModel(it: ResultRow) = WorkshopRegistration(
            it[id].value,
            it[userId].value,
            it[workshopId],
            it[createdAt],
            it[updatedAt],
            it[state],
        )

        fun toDTO(it: ResultRow) = WorkshopRegistrationDTO(
            workshopId = it[workshopId],
            workshopTitle = it[WorkshopRepository.WorkshopTable.title],
            workshopStartTime = TimeUtil.toGmtPlus2(it[WorkshopRepository.WorkshopTable.startTime]),
            workshopEndTime = TimeUtil.toGmtPlus2(it[WorkshopRepository.WorkshopTable.endTime]),
            state = it[state],
        )
    }

    suspend fun create(workshopId: String, userId: Int, state: WorkshopRegistrationState): Int = dbQuery {
        WorkshopRegistrationTable.insertAndGetId {
            it[this.userId] = userId
            it[this.workshopId] = workshopId
            it[this.state] = state
            it[createdAt] = Clock.System.now()
            it[updatedAt] = Clock.System.now()
        }.value
    }

    suspend fun getByUserId(userId: Int): List<WorkshopRegistrationDTO> = dbQuery {
        ( WorkshopRegistrationTable innerJoin WorkshopRepository.WorkshopTable )
            .select(
                WorkshopRepository.WorkshopTable.title,
                WorkshopRepository.WorkshopTable.startTime,
                WorkshopRepository.WorkshopTable.endTime,
                WorkshopRegistrationTable.state,
                WorkshopRegistrationTable.workshopId
            ).where { WorkshopRegistrationTable.userId eq userId }
            .map(WorkshopRegistrationTable::toDTO)
    }

    suspend fun getByWorkshopAndUser(workshopId: String, userId: Int): WorkshopRegistration? {
        return dbQuery {
            WorkshopRegistrationTable.selectAll()
                .where { (WorkshopRegistrationTable.workshopId eq workshopId) and (WorkshopRegistrationTable.userId eq userId) }
                .map(WorkshopRegistrationTable::toModel).singleOrNull()
        }
    }


    suspend fun getByWorkshop(workshopId: String): List<WorkshopRegistration> {
        return dbQuery {
            WorkshopRegistrationTable.selectAll().where { WorkshopRegistrationTable.workshopId eq workshopId }.forUpdate()
                .map(WorkshopRegistrationTable::toModel)
        }
    }

    suspend fun updateState(id: Int, state: WorkshopRegistrationState) {
        return dbQuery {
            WorkshopRegistrationTable.update({ WorkshopRegistrationTable.id eq id }) {
                it[WorkshopRegistrationTable.state] = state
            }
        }
    }
}
