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
 * Represents the `Users` table in the database. This table is used to store information about users, including their
 * chat ID, username, state, admin status, private mode status, and the time when the user record was created. Each user
 * is uniquely identified by their chat ID.
 *
 * ## Columns:
 * - `chatId`: A long value representing the unique chat ID of the user. This column has a unique index.
 * - `username`: A varchar column with a maximum length of 50 characters, representing the user's username.
 * - `state`: A varchar column with a maximum length of 50 characters, representing the user's current state.
 * - `isAdmin`: A boolean column that indicates whether the user has admin privileges. Defaults to `false`.
 * - `privateMode`: A boolean column that indicates whether the user has enabled private mode. Defaults to `false`.
 * - `created`: A datetime column that stores the timestamp of when the user record was created. Defaults to the current
 *   system time.
 *
 * ## Primary Key:
 * The primary key for this table is the `chatId` column, which is represented by the `id` property in the table object.
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

    // Custom primary key definition
    override val id: Column<EntityID<Long>> = chatId.entityId()
}
