package no.javabin.repository

import no.javabin.dto.SpeakerDTO
import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import org.jetbrains.exposed.sql.*

data class Speaker(
    val name: String,
    val bio: String,
    val twitter: String,
    val workshopId: String,
    val index: Int,
) {
    fun toDTO() = SpeakerDTO(name, bio, twitter)
}

class SpeakerRepository {

    internal object SpeakerTable : Table("speaker") {
        val name = varchar("full_name", 256)
        val bio = varchar("bio", 2048)
        val twitter = varchar("twitter", 256)
        val workshopId = reference("workshop_id", WorkshopRepository.WorkshopTable.id)
        val index = integer("index");

        override val primaryKey = PrimaryKey(arrayOf(workshopId, index), "id")

        fun toModel(it: ResultRow) = Speaker(
            name = it[name],
            bio = it[bio],
            twitter = it[twitter],
            workshopId = it[workshopId],
            index = it[index]
        )
    }

    suspend fun list(): List<Speaker> = dbQuery {
        SpeakerTable.selectAll()
            .map(SpeakerTable::toModel)
    }


    private suspend fun create(speakers: List<Speaker>) = dbQuery {
        SpeakerTable.batchInsert(speakers) { speaker ->
            this[SpeakerTable.name] = speaker.name
            this[SpeakerTable.bio] = speaker.bio
            this[SpeakerTable.twitter] = speaker.twitter
            this[SpeakerTable.workshopId] = speaker.workshopId
            this[SpeakerTable.index] = speaker.index
        }
    }



    suspend fun replace(speakers: List<Speaker>) {

        dbQuery {
            SpeakerTable.deleteAll()
            create(speakers)

        }
    }
}
