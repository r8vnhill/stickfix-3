package cl.ravenhill.stickfix.db.schema

import cl.ravenhill.stickfix.db.schema.Users.chatId
import cl.ravenhill.stickfix.db.schema.Users.id
import cl.ravenhill.stickfix.db.schema.Users.isAdmin
import cl.ravenhill.stickfix.db.schema.Users.privateMode
import cl.ravenhill.stickfix.db.schema.Users.state
import cl.ravenhill.stickfix.db.schema.Users.username
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

private const val MAX_USERNAME_LENGTH = 50
private const val MAX_STATE_LENGTH = 50

/**
 * Represents the `Users` table in the database, used to store information about users interacting with the Stickfix
 * bot. This table includes various columns to capture the user's ID, username, state, and other settings related to
 * their interactions with the bot.
 *
 * ## Columns:
 * - `chatId`: A unique identifier for the user, corresponding to their chat ID in Telegram. This is used as the primary
 *   key for the table.
 * - `username`: A string representing the username of the user, with a maximum length defined by `MAX_USERNAME_LENGTH`.
 * - `state`: A string representing the current state of the user within the bot's state machine, with a maximum length
 *   defined by `MAX_STATE_LENGTH`.
 * - `isAdmin`: A boolean flag indicating whether the user has administrative privileges. Defaults to `false`.
 * - `privateMode`: A boolean flag indicating whether the user is in private mode, where all their stickers are private.
 *   Defaults to `false`.
 * - `created`: A timestamp indicating when the user was created in the database. Defaults to the current date and time
 *   when the user is first added.
 * - `shuffle`: A boolean flag indicating whether shuffle mode is enabled for the user. Defaults to `false`.
 *
 * ## Primary Key:
 * - The primary key for this table is defined as `chatId`, which is a unique identifier for each user in the database.
 *
 * ## Usage:
 * This table is used within the Stickfix bot application to track user data and manage interactions. It supports
 * various operations such as querying user information, updating user states, and managing user-specific settings like
 * private mode and shuffle mode.
 */
object Users : IdTable<Long>() {
    // Column definitions
    val chatId = long("chat_id").uniqueIndex()
    val username = varchar("username", MAX_USERNAME_LENGTH)
    val state = varchar("state", MAX_STATE_LENGTH)
    val isAdmin = bool("is_admin").default(false)
    val privateMode = bool("private_mode").default(false)
    val created = datetime("created").default(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )
    val shuffle = bool("shuffle").default(false)

    // Custom primary key definition
    override val id: Column<EntityID<Long>> = chatId.entityId()
}
