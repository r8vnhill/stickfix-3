package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.jakt.Jakt.constraints
import cl.ravenhill.jakt.constrainedTo
import cl.ravenhill.jakt.constraints.BeNull
import cl.ravenhill.stickfix.HaveSize
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.logTrace
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.SealedState
import cl.ravenhill.stickfix.states.resolveState
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Defines the core operations and properties required for interacting with a database. This interface
 * provides a contract for implementing classes to manage user data and perform database operations safely
 * and efficiently.
 *
 * @property database The database instance that the service will use to perform operations.
 */
interface DatabaseService {
    val database: Database
    private val logger: Logger get() = LoggerFactory.getLogger(javaClass)

    /**
     * Retrieves user information based on the given user ID. This method performs a query to fetch
     * the user's data from the database and applies constraints to ensure the data's validity. If the
     * user is found and meets all constraints, a `StickfixUser` object is returned. Otherwise, the method
     * returns an appropriate error message.
     *
     * @param userId The ID of the user to be retrieved.
     * @return An `Either` type representing the result of the query operation. On success, it returns
     * a `DatabaseOperationSuccess` containing the retrieved `StickfixUser`. On failure, it returns
     * a `DatabaseOperationFailure` with an appropriate error message and exception.
     */
    fun getUser(userId: Long): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        executeDatabaseOperationSafely(database) {
            Users.selectAll().where { Users.id eq userId }.constrainedTo { query ->
                "User must be present in the database" { query must HaveSize { it > 0 } }
            }.single().constrainedTo {
                "User must have an ID" { it.getOrNull(Users.id) mustNot BeNull }
                "User must have a username" { it.getOrNull(Users.username) mustNot BeNull }
                "User must have a state" { it.getOrNull(Users.state) mustNot BeNull }
                "User must have a private mode" { it.getOrNull(Users.privateMode) mustNot BeNull }
            }.let { row ->
                StickfixUser(row[Users.username], row[Users.chatId]).apply {
                    state = resolveState(row[Users.state], this)
                }
            }
        }

    /**
     * Adds a new user to the database.
     *
     * @param user The `StickfixUser` instance representing the new user.
     * @return The result of the add operation, indicating success or failure, along with the added user data.
     */
    fun addUser(user: StickfixUser): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        executeDatabaseOperationSafely(database) {
            constraints {
                "User must not be present in the database" {
                    Users.selectAll().where { Users.id eq user.id } must HaveSize { it == 0L }
                }
            }
            Users.insert {
                it[chatId] = user.id
                it[username] = user.username
                it[state] = IdleState::class.simpleName!!
            }
            StickfixUser(user.username, user.id)
        }

    /**
     * Sets the state of a user in the database. This function updates the user's state both in-memory and in the
     * database, ensuring that the user's state is consistently managed.
     *
     * @param state The new state to set for the user. This state is represented as an instance of the `State`
     *   interface.
     * @return `DatabaseOperationResult` indicating the success or failure of the operation. On success, it returns a
     *   `DatabaseOperationSuccess` with the updated state. On failure, it returns a `DatabaseOperationFailure`with the
     *   appropriate error message and exception.
     */
    fun setUserState(
        user: StickfixUser,
        state: (StickfixUser) -> SealedState,
    ): Either<DatabaseOperationFailure, DatabaseOperationSuccess<SealedState>> =
        executeDatabaseOperationSafely(database) {
            constraints {
                "User must be present in the database" {
                    Users.selectAll().where { Users.id eq user.id } must HaveSize { it > 0 }
                }
            }
            val newState = state(user)
            logTrace(logger) { "Setting user $user.debugInfo state to ${newState::class.simpleName}" }
            user.state = newState
            Users.update({ Users.id eq user.id }) {
                it[this.state] = newState::class.simpleName!!
            }
            newState
        }

    /**
     * Deletes a user from the database based on the given `StickfixUser` instance.
     *
     * @param user The `StickfixUser` instance representing the user to be deleted.
     */
    fun deleteUser(user: StickfixUser): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        executeDatabaseOperationSafely(database) {
            Users.deleteWhere { id eq user.id }
            user
        }
}
