package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.jakt.Jakt.constraints
import cl.ravenhill.jakt.constraints.BeNull
import cl.ravenhill.jakt.constraints.longs.BeEqualTo
import cl.ravenhill.stickfix.HaveSize
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.logTrace
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.SealedState
import cl.ravenhill.stickfix.states.resolveState
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
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
     * Retrieves user information based on the given user ID.
     *
     * @param userId The ID of the user to be retrieved.
     * @return An `Either` type representing the result of the query operation. On success, it returns
     * a `DatabaseOperationSuccess` containing the retrieved `StickfixUser`. On failure, it returns
     * a `DatabaseOperationFailure` with an appropriate error message and exception.
     */
    fun getUser(userId: Long): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        executeDatabaseOperationSafely(database) {
            checkUserExists(userId)
            Users.selectAll().where { Users.id eq userId }
                .single().let { row ->
                    validateUserRow(row)
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
            ensureUserNotExists(user.id)
            Users.insert {
                it[chatId] = user.id
                it[username] = user.username
                it[state] = IdleState::class.simpleName!!
            }
            user
        }

    /**
     * Sets the state of a user in the database.
     *
     * @param state The new state to set for the user.
     * @return `DatabaseOperationResult` indicating the success or failure of the operation.
     */
    fun setUserState(
        user: StickfixUser,
        state: (StickfixUser) -> SealedState,
    ): Either<DatabaseOperationFailure, DatabaseOperationSuccess<SealedState>> =
        executeDatabaseOperationSafely(database) {
            constraints {
                "Cannot set user state of default user" { user.id mustNot BeEqualTo(0L) }
            }
            checkUserExists(user.id)
            val newState = state(user)
            logUserStateChange(user, newState)
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
            checkUserExists(user.id)
            Users.deleteWhere { id eq user.id }
            user
        }

    /**
     * Checks if a user with the specified `userId` exists in the database. This function applies a constraint to ensure
     * that the user is present in the database. If the user is not found, a constraint violation will be triggered.
     *
     * @param userId The ID of the user to check for existence in the database.
     */
    private fun checkUserExists(userId: Long) = constraints {
        "User must be present in the database" {
            Users.selectAll().where { Users.id eq userId } must HaveSize { it > 0 }
        }
    }

    /**
     * Ensures that a user with the specified `userId` does not exist in the database. This function applies a
     * constraint to verify that the user is not present. If the user is found, a constraint violation will be
     * triggered.
     *
     * @param userId The ID of the user to check for non-existence in the database.
     */
    private fun ensureUserNotExists(userId: Long) = constraints {
        "User must not be present in the database" {
            Users.selectAll().where { Users.id eq userId } must HaveSize { it == 0L }
        }
    }

    /**
     * Validates the integrity of a `ResultRow` representing a user. This function applies a series of constraints to
     * ensure that the row contains all necessary fields (ID, username, state, and private mode). If any of these fields
     * are missing or null, a constraint violation will be triggered.
     *
     * @param row The `ResultRow` to validate.
     */
    private fun validateUserRow(row: ResultRow) = constraints {
        "User must have an ID" { row.getOrNull(Users.id) mustNot BeNull }
        "User must have a username" { row.getOrNull(Users.username) mustNot BeNull }
        "User must have a state" { row.getOrNull(Users.state) mustNot BeNull }
        "User must have a private mode" { row.getOrNull(Users.privateMode) mustNot BeNull }
    }

    /**
     * Logs the state change of a user. This function logs a trace message indicating that the user's state is being
     * updated to a new state. The message includes the user's debug information and the name of the new state.
     *
     * @param user The `StickfixUser` whose state is being changed.
     * @param newState The new state to which the user is being transitioned.
     */
    private fun logUserStateChange(user: StickfixUser, newState: SealedState) {
        logTrace(logger) { "Setting user ${user.debugInfo} state to ${newState::class.simpleName}" }
    }
}
