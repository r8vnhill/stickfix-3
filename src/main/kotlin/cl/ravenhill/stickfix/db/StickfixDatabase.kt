/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.jakt.constrainedTo
import cl.ravenhill.jakt.constraints.BeNull
import cl.ravenhill.stickfix.HaveSize
import cl.ravenhill.stickfix.PrivateMode
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.State
import cl.ravenhill.stickfix.states.resolveState
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

/**
 * Represents the database operations and interactions for the Stickfix bot application. This class provides methods to
 * initialize the database, query for specific data, and perform CRUD operations on the user data.
 *
 * @property jdbcUrl The JDBC URL for connecting to the database.
 * @property driverName The name of the database driver.
 */
class StickfixDatabase(private val jdbcUrl: String, private val driverName: String) {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

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
     * Retrieves user information based on the given `StickfixUser` instance.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @return The result of the query operation, indicating success or failure, along with the retrieved user data.
     */
    fun getUser(user: StickfixUser): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
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
            }.single().constrainedTo {
                "User must have an ID" { it.getOrNull(Users.id) mustNot BeNull }
                "User must have a username" { it.getOrNull(Users.username) mustNot BeNull }
                "User must have a state" { it.getOrNull(Users.state) mustNot BeNull }
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
            Users.insert {
                it[chatId] = user.userId
                it[username] = user.username
                it[state] = IdleState::class.simpleName!!
            }
            StickfixUser(user.username, user.userId)
        }

    /**
     * Deletes a user from the database based on the given `StickfixUser` instance.
     *
     * @param user The `StickfixUser` instance representing the user to be deleted.
     */
    fun deleteUser(user: StickfixUser): Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>> =
        executeDatabaseOperationSafely(database) {
            Users.deleteWhere { id eq user.userId }
            user
        }

    /**
     * Sets the private mode for a user in the database.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @param mode The `PrivateMode` enum value indicating whether private mode is enabled or disabled.
     * @return The result of the update operation, indicating success or failure, along with the new private mode
     *   setting.
     */
    fun setPrivateMode(user: StickfixUser, mode: PrivateMode) = executeDatabaseOperationSafely(database) {
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
     * @param user The `StickfixUser` instance representing the user.
     */
    inline fun <reified T> setUserState(user: StickfixUser) = executeDatabaseOperationSafely(database) {
        val logger = LoggerFactory.getLogger(javaClass.simpleName)
        logInfo(logger) { "Setting user ${user.userId} state to ${T::class.simpleName}" }
        Users.update({ Users.id eq user.userId }) {
            it[state] = T::class.simpleName!!
        }
    }

    /**
     * Sets the state of a user in the database. This function updates the user's state both in-memory and in the database,
     * ensuring that the user's state is consistently managed.
     *
     * @param state The new state to set for the user. This state is represented as an instance of the `State` interface.
     * @return `DatabaseOperationResult` indicating the success or failure of the operation. On success, it returns a
     *         `DatabaseOperationSuccess` with the updated state. On failure, it returns a `DatabaseOperationFailure` with
     *         the appropriate error message and exception.
     */
    fun setUserState(state: State): Either<DatabaseOperationFailure, DatabaseOperationSuccess<Int>> =
        executeDatabaseOperationSafely(database) {
            val user = state.user
            logInfo(logger) { "Setting user ${user.userId} state to ${state::class.simpleName}" }
            user.state = state
            Users.update({ Users.id eq user.userId }) {
                it[this.state] = state::class.simpleName!!
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
