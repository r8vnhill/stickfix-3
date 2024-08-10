/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.jakt.constrainedTo
import cl.ravenhill.stickfix.HaveSize
import cl.ravenhill.stickfix.PrivateMode
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
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
class StickfixDatabase(private val jdbcUrl: String, private val driverName: String) : DatabaseService {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    /**
     * The database instance used for performing database operations. This property is initialized during the `init`
     * method call.
     */
    override lateinit var database: Database
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
        getUser(user.id)

    /**
     * Sets the private mode for a user in the database.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @param mode The `PrivateMode` enum value indicating whether private mode is enabled or disabled.
     * @return The result of the update operation, indicating success or failure, along with the new private mode
     *   setting.
     */
    fun setPrivateMode(user: StickfixUser, mode: PrivateMode) = executeDatabaseOperationSafely(database) {
        Users.update({ Users.id eq user.id }) {
            it[privateMode] = when (mode) {
                PrivateMode.ENABLED -> true
                PrivateMode.DISABLED -> false
            }
        }
        mode
    }

    /**
     * Returns a string representation of the `StickfixDatabase` instance, including the JDBC URL and driver name.
     *
     * @return String A string representation of the `StickfixDatabase` instance.
     */
    override fun toString() =
        "StickfixDatabase(jdbcUrl='$jdbcUrl', driverName='$driverName')"
}
