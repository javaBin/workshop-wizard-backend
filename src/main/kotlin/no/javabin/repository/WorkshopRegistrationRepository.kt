package no.javabin.repository

import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import no.javabin.dto.WorkshopRegistrationDTO
import no.javabin.util.TimeUtil
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


enum class WorkshopRegistrationStatus {
    WAIT_LIST, APPROVED, CANCELLED,
}

data class WorkshopRegistration(
    val userId: Int,
    val workshopId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    var status: WorkshopRegistrationStatus = WorkshopRegistrationStatus.WAIT_LIST,
)

class WorkshopRegistrationRepository {
    internal object WorkshopRegistrationTable : Table("workshop_registration") {
        val userId = reference("user_id", UserRepository.UserTable.id)
        val workshopId = reference("workshop_id", WorkshopRepository.WorkshopTable.id)
        val status = enumerationByName<WorkshopRegistrationStatus>("status", 64)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")

        override val primaryKey = PrimaryKey(
            arrayOf(
                userId, workshopId
            ), "id"
        )

        fun toModel(it: ResultRow) = WorkshopRegistration(
            it[userId].value,
            it[workshopId],
            it[createdAt],
            it[updatedAt],
            it[status],
        )

        fun toDTO(it: ResultRow) = WorkshopRegistrationDTO(
            workshopId = it[workshopId],
            workshopTitle = it[WorkshopRepository.WorkshopTable.title],
            workshopStartTime = TimeUtil.toGmtPlus2(it[WorkshopRepository.WorkshopTable.startTime]),
            workshopEndTime = TimeUtil.toGmtPlus2(it[WorkshopRepository.WorkshopTable.endTime]),
            status = it[status],
        )
    }

    suspend fun create(workshopId: String, userId: Int, state: WorkshopRegistrationStatus) = dbQuery {
        WorkshopRegistrationTable.insert {
            it[this.userId] = userId
            it[this.workshopId] = workshopId
            it[this.status] = state
            it[createdAt] = Clock.System.now()
            it[updatedAt] = Clock.System.now()
        }
    }

    suspend fun getByUserId(userId: Int): List<WorkshopRegistrationDTO> = dbQuery {
        ( WorkshopRegistrationTable innerJoin WorkshopRepository.WorkshopTable )
            .select(
                WorkshopRepository.WorkshopTable.title,
                WorkshopRepository.WorkshopTable.startTime,
                WorkshopRepository.WorkshopTable.endTime,
                WorkshopRegistrationTable.status,
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
            WorkshopRegistrationTable.selectAll().where { WorkshopRegistrationTable.workshopId eq workshopId }
                .forUpdate()
                .orderBy(WorkshopRegistrationTable.updatedAt)
                .map(WorkshopRegistrationTable::toModel)
        }
    }

    suspend fun updateStatus(userId: Int, workshopId: String, status: WorkshopRegistrationStatus): Int {
        return dbQuery {
            WorkshopRegistrationTable.update({
                (WorkshopRegistrationTable.userId eq userId) and (WorkshopRegistrationTable.workshopId eq workshopId)
            })
            {
                it[WorkshopRegistrationTable.status] = status
                it[updatedAt] = Clock.System.now()
            }
        }
    }
}
