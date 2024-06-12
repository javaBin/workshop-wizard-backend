package no.javabin.repository

import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import kotlinx.datetime.Instant
import no.javabin.dto.WorkshopRegistrationDTO
import no.javabin.util.TimeUtil
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


enum class WorkshopRegistrationStatus {
    PENDING, WAITLIST, APPROVED, CANCELLED,
}

data class WorkshopRegistration(
    override val id: Int?,
    val userId: Int,
    val workshopId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    var status: WorkshopRegistrationStatus = WorkshopRegistrationStatus.PENDING,
) : Model

class WorkshopRegistrationRepository {
    internal object WorkshopRegistrationTable : IntIdTable("workshop_registration") {
        val userId = reference("user_id", UserRepository.UserTable.id)
        val workshopId = reference("workshop_id", WorkshopRepository.WorkshopTable.id)
        val status = enumerationByName<WorkshopRegistrationStatus>("status", 64)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")

        fun toModel(it: ResultRow) = WorkshopRegistration(
            it[id].value,
            it[userId].value,
            it[workshopId],
            it[createdAt],
            it[updatedAt],
            it[status],
        )

        fun toDTO(it: ResultRow) = WorkshopRegistrationDTO(
            workshopTitle = it[WorkshopRepository.WorkshopTable.title],
            workshopStartTime = TimeUtil.toGmtPlus2(it[WorkshopRepository.WorkshopTable.startTime]),
            workshopEndTime = TimeUtil.toGmtPlus2(it[WorkshopRepository.WorkshopTable.endTime]),
            status = it[status],
        )
    }

    suspend fun create(registration: WorkshopRegistration) = dbQuery {
        WorkshopRegistrationTable.insert {
            it[userId] = registration.userId
            it[workshopId] = registration.workshopId
            it[status] = registration.status
            it[createdAt] = registration.createdAt
            it[updatedAt] = registration.updatedAt
        }
    }

    suspend fun createOrUpdate(registration: WorkshopRegistration) = dbQuery {
        print("DEBUG****${registration.toString()}")
        WorkshopRegistrationTable.insert {
            it[userId] = registration.userId
            it[workshopId] = registration.workshopId
            it[status] = registration.status
            it[createdAt] = registration.createdAt
            it[updatedAt] = registration.updatedAt
        }
    }

    suspend fun list(userId: Int): List<WorkshopRegistrationDTO> = dbQuery {
        (WorkshopRegistrationTable innerJoin WorkshopRepository.WorkshopTable)
            .select(
                WorkshopRepository.WorkshopTable.title,
                WorkshopRepository.WorkshopTable.startTime,
                WorkshopRepository.WorkshopTable.endTime,
                WorkshopRegistrationTable.status
            ).where { WorkshopRegistrationTable.userId eq userId }
            .map(WorkshopRegistrationTable::toDTO)
    }

    suspend fun getByUserId(userId: Int): List<WorkshopRegistration> {
        return dbQuery {
            WorkshopRegistrationTable.selectAll().where { WorkshopRegistrationTable.userId eq userId }
                .map(WorkshopRegistrationTable::toModel)
        }
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
                .orderBy(WorkshopRegistrationTable.updatedAt)
                .map(WorkshopRegistrationTable::toModel)
        }
    }

//    suspend fun getWorkshopCount(workshopId: String): Long {
//        return dbQuery {
//            WorkshopRegistrationTable.selectAll().where { WorkshopRegistrationTable.workshopId eq workshopId }
//                .andWhere { WorkshopRegistrationTable.status eq WorkshopRegistrationStatus.APPROVED }
//                .count()
//        }
//    }

    suspend fun updateStatus(userId: Int, workshopId: String, status: WorkshopRegistrationStatus): Int {
        return dbQuery {
            WorkshopRegistrationTable.update({
                (WorkshopRegistrationTable.userId eq userId) and (WorkshopRegistrationTable.workshopId eq workshopId)
            })
            { it[WorkshopRegistrationTable.status] = status }
        }
    }
}
