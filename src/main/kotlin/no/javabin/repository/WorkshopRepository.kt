package no.javabin.repository

import no.javabin.dto.WorkshopDTO
import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.selectAll

class Workshop(
    override val id: Int,
    val title: String,
    val description: String,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int
) : Model {
    fun toDTO() = WorkshopDTO(title, description, startTime, endTime, capacity)
}

class WorkshopRepository {
    internal object WorkshopTable : IntIdTable() {
        val title = varchar("title", 128)
        val description = varchar("description", 256)
        val startTime = timestamp("start_time")
        val endTime = timestamp("end_time")
        val capacity = integer("capacity")

        fun toModel(it: ResultRow) = Workshop(
            it[id].value,
            it[title],
            it[description],
            it[startTime],
            it[endTime],
            it[capacity]
        )
    }
    suspend fun list(): List<Workshop> = dbQuery {
        WorkshopTable.selectAll()
            .map(WorkshopTable::toModel)
    }
}
