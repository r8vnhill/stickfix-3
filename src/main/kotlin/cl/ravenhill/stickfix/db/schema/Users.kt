package cl.ravenhill.stickfix.db.schema

import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.states.IdleState
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private const val MAX_USERNAME_LENGTH = 50
private const val MAX_STATE_LENGTH = 50

/**
 * Represents the `Users` table in the database, using the Exposed SQL framework. This object extends
 * `IdTable<Long>`, using a long integer as the primary key column, specifically the chat ID in this context,
 * which is unique for each user. It includes columns for storing usernames and the corresponding chat IDs.
 *
 * ## Usage:
 * This object facilitates database interactions by providing a structured way to reference and manipulate user data.
 * It is essential in data access layers or service classes where user information is frequently queried or updated.
 *
 * ### Example 1: Inserting a New User
 * Demonstrates how to insert a new user into the `Users` table:
 * ```kotlin
 * transaction {
 *     Users.insert {
 *         it[username] = "new_user"
 *         it[chatId] = 123456789L
 *     }
 * }
 * ```
 * ### Example 2: Querying Users by Username
 * Illustrates how to retrieve users from the `Users` table by their username:
 * ```kotlin
 * val userNames = transaction {
 *     Users.select { Users.username eq "existing_user" }
 *         .map { it[Users.username] }
 * }
 * userNames.forEach { println(it) }
 * ```
 *
 * The `Users` object is commonly utilized within DAO (Data Access Object) patterns or in service layers
 * to manage and query user data effectively.
 *
 * @property username The column representing the user's username, defined as a VARCHAR of length 50. This is used
 *   to store the user's chosen username.
 * @property chatId The primary key of the table, representing the user's Telegram chat ID as a unique identifier,
 *   defined as a LONG type. This field is used both as a unique identifier and the primary key of the table.
 */
object Users : IdTable<Long>() {
    // Column definitions
    val chatId = long("chat_id")
    val username = varchar("username", MAX_USERNAME_LENGTH)
    val state = varchar("state", MAX_STATE_LENGTH)

    // Custom primary key definition
    override val id: Column<EntityID<Long>> = chatId.entityId()
}

/**
 * Deletes a user from the database. This function encapsulates the operation within a transaction,
 * ensuring atomicity of the delete operation. The deletion is based on the user's ID.
 *
 * @param user
 *  The `ReadUser` instance representing the user to be deleted. The user's ID is used to identify the correct record in
 *  the database.
 */
fun DatabaseService.deleteUser(user: ReadUser) {
    transaction(database) {
        Users.deleteWhere { id eq user.userId }  // Perform the deletion in the database based on the user ID.
    }
}

/**
 * Updates a user's details in the database. This function encapsulates the update operation within a transaction,
 * ensuring the integrity and atomicity of the operation. It specifically updates the username based on the user's ID.
 *
 * @param user
 *  The `ReadUser` instance representing the new state of the user to be updated. The user's ID is used to locate the
 *  existing record, and the provided details (e.g., username) are used to update the record.
 */
fun DatabaseService.updateUser(user: ReadUser) {
    transaction(database) {
        Users.update({ Users.id eq user.userId }) {
            it[username] = user.username
        }
    }
}

/**
 * Adds a new user to the database. This function performs the insertion within a transaction,
 * ensuring that the operation is atomic. It inserts a new record with the user's ID, username,
 * and state into the Users table.
 *
 * @param user
 *  The `ReadUser` instance containing the details of the user to be added. The user's ID is used as the primary key,
 *  the username as a descriptor, and the state is set to 'Idle' by default upon insertion.
 */
fun DatabaseService.addUser(user: ReadUser) {
    transaction(database) {
        Users.insert {
            it[chatId] = user.userId  // Assigns the user's ID to the chatId column.
            it[username] = user.username  // Sets the username field.
            it[state] = IdleState::class.simpleName!!  // Initializes the state to 'Idle'.
        }
    }
}

/**
 * Retrieves a user from the database based on their user ID. This function performs the retrieval within a transaction
 * and returns the corresponding `ReadUser` instance if the user exists, or `null` if no such user is found.
 *
 * @param user
 *  The `ReadUser` instance containing the ID of the user to retrieve. Only the `userId` is used in this function to
 *  identify the user in the database.
 * @return The `ReadUser` instance if found, or `null` if no user with the given ID exists.
 */
fun DatabaseService.getUser(user: ReadUser): ReadUser? = transaction(database) {
    val result = Users.selectAll().where { Users.id eq user.userId }
    if (result.count() == 0L) {
        null
    } else {
        StickfixUser.from(result.single())
    }
}
