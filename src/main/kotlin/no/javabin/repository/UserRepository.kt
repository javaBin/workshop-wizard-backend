package no.javabin.repository

import no.javabin.dto.UserDTO
import com.inventy.plugins.DatabaseFactory.Companion.dbQuery
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

class User(
    override val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val imageUrl: String?,
    val isAdmin: Boolean,
) : Model {

    fun toDTO(): UserDTO {
        return UserDTO(
            firstName,
            lastName,
            email,
            imageUrl ?: "",
            isAdmin,
        )
    }
}

class UserRepository{
    internal object UserTable : IntIdTable("\"user\"") {
        val firstName = varchar("first_name", 256)
        val lastName = varchar("last_name", 256)
        val email = varchar("email", 256)
        val imageUrl = varchar("image_url", 256)
        val isAdmin = bool("is_admin")

        fun toModel(it: ResultRow) = User(
            it[id].value,
            it[firstName],
            it[lastName],
            it[email],
            it[imageUrl],
            it[isAdmin]
        )

    }

    suspend fun create(user: UserDTO): Int = dbQuery {
        UserTable.insertAndGetId {
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[email] = user.email
            it[imageUrl] = user.imageUrl
            it[isAdmin] = user.isAdmin
        }.value
    }

    suspend fun list(): List<User> = dbQuery {
        UserTable.selectAll()
            .map(UserTable::toModel)
    }

    suspend fun readByEmail(email: String): User? {
        return dbQuery {
            UserTable.selectAll().where { UserTable.email eq email }
                .map(UserTable::toModel)
                .firstOrNull()
        }
    }
}
