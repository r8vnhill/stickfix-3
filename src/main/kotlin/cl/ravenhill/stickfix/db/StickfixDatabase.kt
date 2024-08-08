/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import cl.ravenhill.jakt.constrainedTo
import cl.ravenhill.jakt.exceptions.CompositeException
import cl.ravenhill.stickfix.HaveSize
import cl.ravenhill.stickfix.PrivateMode
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.exceptions.DatabaseOperationException
import cl.ravenhill.stickfix.info
import cl.ravenhill.stickfix.states.IdleState
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.sql.SQLException

/**
 * Represents the database operations and interactions for the Stickfix bot application. This class provides methods to
 * initialize the database, query for specific data, and perform CRUD operations on the user data.
 *
 * @property jdbcUrl The JDBC URL for connecting to the database.
 * @property driverName The name of the database driver.
 */
class StickfixDatabase(private val jdbcUrl: String, private val driverName: String) {

    /**
     * The database instance used for performing database operations. This property is initialized during the `init`
     * method call.
     */
    lateinit var database: Database
        private set

    /**
     * Initializes the database by connecting to the specified JDBC URL and driver, and creating the necessary schema
     * tables.
     *
     * @return The result of the initialization operation, indicating success or failure, along with the initialized
     *   `StickfixDatabase` instance.
     */
    fun init(): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixDatabase>> {
        database = Database.connect(jdbcUrl, driverName)
        return executeDatabaseOperationSafely(database) {
            SchemaUtils.create(Meta, Users)
            this@StickfixDatabase
        }
    }

    /**
     * Queries the database for the API key.
     *
     * @return Either<DatabaseOperationFailure, DatabaseOperationSuccess<String>> The result of the query operation,
     *         indicating success or failure, along with the retrieved API key.
     */
    fun queryApiKey(): Either<DatabaseOperationFailure, DatabaseOperationSuccess<String>> =
        executeDatabaseOperationSafely(database) {
            Meta.selectAll().where { Meta.key eq "API_KEY" }.constrainedTo { query ->
                "API_KEY must be present in meta table" { query must HaveSize { it > 0 } }
            }.single()[Meta.value]
        }

    /**
     * Retrieves user information based on the given `ReadUser` instance.
     *
     * @param user The `ReadUser` instance representing the user.
     * @return The result of the query operation, indicating success or failure, along with the retrieved user data.
     */
    fun getUser(user: ReadUser): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        getUser(user.userId)

    /**
     * Retrieves user information based on the given user ID.
     *
     * @param userId The ID of the user to be retrieved.
     * @return The result of the query operation, indicating success or failure, along with the retrieved user data.
     */
    fun getUser(userId: Long): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        executeDatabaseOperationSafely(database) {
            Users.selectAll().where { Users.id eq userId }.constrainedTo { query ->
                "User must be present in the database" { query must HaveSize { it > 0 } }
            }.single().let(StickfixUser.Companion::from)
        }

    /**
     * Adds a new user to the database.
     *
     * @param user The `ReadUser` instance representing the new user.
     * @return The result of the add operation, indicating success or failure, along with the added user data.
     */
    fun addUser(user: ReadUser): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        executeDatabaseOperationSafely(database) {
            Users.insert {
                it[chatId] = user.userId
                it[username] = user.username
                it[state] = IdleState::class.simpleName!!
            }
            StickfixUser(user.username, user.userId)
        }

    /**
     * Deletes a user from the database based on the given `ReadUser` instance.
     *
     * @param user The `ReadUser` instance representing the user to be deleted.
     */
    fun deleteUser(user: ReadUser): Either<DatabaseOperationFailure, DatabaseOperationSuccess<ReadUser>> =
        executeDatabaseOperationSafely(database) {
            Users.deleteWhere { id eq user.userId }
            user
        }

    /**
     * Sets the private mode for a user in the database.
     *
     * @param user The `ReadUser` instance representing the user.
     * @param mode The `PrivateMode` enum value indicating whether private mode is enabled or disabled.
     * @return The result of the update operation, indicating success or failure, along with the new private mode
     *   setting.
     */
    fun setPrivateMode(user: ReadUser, mode: PrivateMode) = executeDatabaseOperationSafely(database) {
        Users.update({ Users.id eq user.userId }) {
            it[privateMode] = when (mode) {
                PrivateMode.ENABLED -> true
                PrivateMode.DISABLED -> false
            }
        }
        mode
    }


    /**
     * Sets the state of a user in the database to the specified state type. This function updates the user's state to
     * the simple name of the specified class type `T`.
     *
     * @param T The type of the state to set for the user. This must be a class type.
     * @param userId The ID of the user whose state is to be updated.
     */
    inline fun <reified T> setUserState(userId: Long) = executeDatabaseOperationSafely(database) {
        val logger = LoggerFactory.getLogger(javaClass.simpleName)
        info(logger) { "Setting user $userId state to ${T::class.simpleName}" }
        Users.update({ Users.id eq userId }) {
            it[state] = T::class.simpleName!!
        }
    }

    /**
     * Returns a string representation of the `StickfixDatabase` instance, including the JDBC URL and driver name.
     *
     * @return String A string representation of the `StickfixDatabase` instance.
     */
    override fun toString() =
        "StickfixDatabase(jdbcUrl='$jdbcUrl', driverName='$driverName')"
}

/**
 * Executes a database operation within a transaction and handles any potential exceptions that may occur during the
 * operation. This function ensures that any database operation is executed safely, returning a result indicating
 * success or failure.
 *
 * @param T The type of the result produced by the successful database operation.
 * @param database The `Database` instance used to execute the database operation.
 * @param successful A lambda function representing the successful database operation to be executed within the
 *   transaction context.
 * @return The result of the database operation, wrapped in an `Either` type to indicate success or failure. On
 *   success, it returns a `DatabaseOperationSuccess` with a success message and the result of the operation. On
 *   failure, it returns a `DatabaseOperationFailure` with the appropriate error message and exception.
 */
fun <T> executeDatabaseOperationSafely(
    database: Database,
    successful: Transaction.() -> T,
): Either<DatabaseOperationFailure, DatabaseOperationSuccess<T>> = transaction(database) {
    try {
        DatabaseOperationSuccess("Database operation completed successfully.", successful()).right()
    } catch (e: SQLException) {
        handleDatabaseException(e).left()
    } catch (e: CompositeException) {
        handleDatabaseException(e).left()
    }
}

/**
 * Handles the exceptions that occur during the database operation, creating a `DatabaseOperationFailure`.
 *
 * @param e The exception that was thrown during the database operation.
 * @return `DatabaseOperationFailure` with an appropriate error message and exception.
 */
private fun handleDatabaseException(
    e: Exception,
) = DatabaseOperationFailure(
    "Database operation failed.",
    DatabaseOperationException(e.message ?: "Unknown error.")
)
