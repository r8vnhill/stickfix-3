package cl.ravenhill.stickfix.db.schema

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

private const val MAX_USERNAME_LENGTH = 50
private const val MAX_STATE_LENGTH = 50

/**
 * Represents the database table for storing user information. This object extends `IdTable` to define the columns and
 * primary key for the table. It includes various columns for storing user details, such as chat ID, username, state,
 * and administrative settings.
 *
 * ## Columns:
 * - `chatId`: The unique chat ID of the user. This serves as the primary key for the table.
 * - `username`: The username of the user, stored as a string with a maximum length defined by `MAX_USERNAME_LENGTH`.
 * - `state`: The current state of the user, stored as a string with a maximum length defined by `MAX_STATE_LENGTH`.
 * - `isAdmin`: A boolean flag indicating whether the user has administrative privileges. Defaults to `false`.
 * - `privateMode`: A boolean flag indicating whether the user is in private mode. Defaults to `false`.
 *
 * ### Example 1: Selecting Users
 * ```kotlin
 * val users = Users.selectAll().map { it[Users.username] }
 * ```
 *
 * ### Example 2: Inserting a User
 * ```kotlin
 * Users.insert {
 *     it[chatId] = 123456789L
 *     it[username] = "exampleUser"
 *     it[state] = "active"
 *     it[isAdmin] = true
 *     it[privateMode] = false
 * }
 * ```
 *
 * @property chatId The unique chat ID of the user. This column is used as the primary key.
 * @property username The username of the user. This column has a maximum length defined by `MAX_USERNAME_LENGTH`.
 * @property state The current state of the user. This column has a maximum length defined by `MAX_STATE_LENGTH`.
 * @property isAdmin A boolean flag indicating whether the user has administrative privileges. Defaults to `false`.
 * @property privateMode A boolean flag indicating whether the user is in private mode. Defaults to `false`.
 * @property id The primary key column, which is an entity ID based on the `chatId`.
 */
object Users : IdTable<Long>() {
    // Column definitions
    val chatId = long("chat_id")
    val username = varchar("username", MAX_USERNAME_LENGTH)
    val state = varchar("state", MAX_STATE_LENGTH)
    val isAdmin = bool("is_admin").default(false)
    val privateMode = bool("private_mode").default(false)

    // Custom primary key definition
    override val id: Column<EntityID<Long>> = chatId.entityId()
}
