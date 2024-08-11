/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.jakt.constrainedTo
import cl.ravenhill.stickfix.HaveSize
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.modes.PrivateMode
import cl.ravenhill.stickfix.modes.ShuffleMode
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

/**
 * Represents the database operations and interactions for the Stickfix bot application. This class provides methods to
 * initialize the database, query for specific data, and perform CRUD operations on the user data.
 *
 * @property jdbcUrl The JDBC URL for connecting to the database.
 * @property driverName The name of the database driver.
 */
class StickfixDatabase(private val jdbcUrl: String, private val driverName: String) : DatabaseService {

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

    // region : Setters for user data. These functions update the database with the provided user data. They are used to
    //         update the user's private mode and shuffle settings.
    /**
     * Sets the private mode for a user in the `Users` table. This function uses the `updateBooleanField` method to update
     * the `privateMode` field in the database based on the provided `PrivateMode` enum value.
     *
     * @param user The `StickfixUser` instance representing the user whose private mode is to be updated.
     * @param mode The `PrivateMode` enum value indicating whether private mode should be enabled or disabled.
     * @return Either a `DatabaseOperationFailure` indicating the failure of the operation, or a `DatabaseOperationSuccess`
     *   indicating the success of the operation, along with the updated private mode setting.
     */
    fun setPrivateMode(user: StickfixUser, mode: PrivateMode) =
        updateBooleanField(user, Users.privateMode, mode == PrivateMode.ENABLED)

    /**
     * Sets the shuffle mode for a user in the `Users` table. This function uses the `updateBooleanField` method to update
     * the `shuffle` field in the database based on the provided `ShuffleMode` enum value.
     *
     * @param user The `StickfixUser` instance representing the user whose shuffle mode is to be updated.
     * @param enabled The `ShuffleMode` enum value indicating whether shuffle mode should be enabled or disabled.
     * @return Either a `DatabaseOperationFailure` indicating the failure of the operation, or a `DatabaseOperationSuccess`
     *   indicating the success of the operation, along with the updated shuffle mode setting.
     */
    fun setShuffle(user: StickfixUser, enabled: ShuffleMode) =
        updateBooleanField(user, Users.shuffle, enabled == ShuffleMode.ENABLED)

    /**
     * Updates a boolean field in the `Users` table for a specific user. This function is generalized to handle updates
     * to any boolean field, ensuring that the specified field is updated with the provided value. The operation is
     * executed within a transaction, and any exceptions encountered during the operation are handled safely.
     *
     * @param user The `StickfixUser` instance representing the user whose record is to be updated.
     * @param field The `Column<Boolean>` representing the specific boolean field in the `Users` table to be updated.
     * @param value The boolean value to set for the specified field.
     * @return Either a `DatabaseOperationFailure` indicating the failure of the operation or a
     *   `DatabaseOperationSuccess` indicating the success of the operation, along with the updated boolean value.
     */
    private fun updateBooleanField(
        user: StickfixUser,
        field: Column<Boolean>,
        value: Boolean,
    ): Either<DatabaseOperationFailure, DatabaseOperationSuccess<Boolean>> = executeDatabaseOperationSafely(database) {
        Users.update({ Users.id eq user.id }) {
            it[field] = value
        }
        value
    }
    // endregion

    // region : Utility functions
    override fun toString() =
        "StickfixDatabase(jdbcUrl='$jdbcUrl', driverName='$driverName')"
    // endregion
}
