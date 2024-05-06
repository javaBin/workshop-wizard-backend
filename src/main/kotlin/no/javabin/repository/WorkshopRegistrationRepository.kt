package no.javabin.repository

import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.select


enum class WorkshopRegistrationState {
    PENDING, WAITLIST, APPROVED, CANCELED,
}

class WorkshopRegistration(
    override val id: Int,
    val userId: Int,
    val workshopId: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    var state: WorkshopRegistrationState = WorkshopRegistrationState.PENDING,
) : Model

class WorkshopRegistrationRepository(userId: Int) : UserOwnedRepository(userId) {
    private object WorkshopRegistrationTable : IntIdTable() {
        val userId = reference("userId", UserRepository.UserTable.id)
        val workshopId = reference("workshop_id", WorkshopRepository.WorkshopTable.id)
        val state = enumerationByName<WorkshopRegistrationState>("state", 64)
        val createdAt = timestamp("created_at")
        val updatedAt = timestamp("updated_at")

        fun toModel(it: ResultRow) = WorkshopRegistration(
            it[id].value,
            it[userId].value,
            it[workshopId].value,
            it[createdAt],
            it[updatedAt],
            it[state],
        )
    }

    suspend fun create(registration: WorkshopRegistration): Int = dbQuery {
        WorkshopRegistrationTable.insertAndGetId {
            it[userId] = registration.userId
            it[workshopId] = registration.workshopId
            it[state] = registration.state
            it[createdAt] = registration.createdAt
            it[updatedAt] = registration.updatedAt
        }.value
    }

    suspend fun list(): List<WorkshopRegistration> = dbQuery {
        WorkshopRegistrationTable.select{ WorkshopRegistrationTable.userId eq userId }
            .map(WorkshopRegistrationTable::toModel)
    }

    suspend fun getByUserId(userId: Int): List<WorkshopRegistration> {
        return dbQuery {
            WorkshopRegistrationTable.select { WorkshopRegistrationTable.userId eq userId }
                .map(WorkshopRegistrationTable::toModel)
        }
    }
}
