/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.PrivateMode
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.states.IdleState
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Represents the Stickfix database, providing functionalities for initializing the database, managing the API token,
 * and handling user data operations. This class integrates with an SQL database using the provided JDBC URL and driver
 * name.
 *
 * @property jdbcUrl The JDBC URL used to connect to the database.
 * @property driverName The name of the database driver to be used for the connection.
 */
class StickfixDatabase(private val jdbcUrl: String, private val driverName: String) {

    /**
     * The database instance used for performing database operations.
     */
    lateinit var database: Database
        private set

    /**
     * Initializes the database, creating necessary tables and setting up the connection.
     *
     * @return DatabaseOperationResult<StickfixDatabase> The result of the initialization operation, indicating success
     *   or failure along with the database instance.
     */
    fun init(): DatabaseOperationResult<StickfixDatabase> {
        database = Database.connect(jdbcUrl, driverName)
        transaction(database) {
            SchemaUtils.create(Meta, Users)
        }
        return DatabaseOperationSuccess("Database initialized successfully.", this)
    }

    /**
     * Manages the API token used for authentication and authorization.
     *
     * @return String The current API token stored in the database.
     */
    var apiToken: String
        get() = transaction(database) {
            Meta.selectAll().single()[Meta.key]
        }
        set(value) {
            transaction(database) {
                Meta.update { row ->
                    row[key] = "API_KEY"
                    row[Meta.value] = value
                }
            }
        }

    /**
     * Retrieves user details based on the provided user information.
     *
     * @param user A `ReadUser` instance containing minimal user information for which details are to be retrieved.
     * @return ReadUser? The user details if found, or null if no user matches the provided information.
     */
    fun getUser(user: ReadUser): DatabaseOperationResult<StickfixUser?> =
        DatabaseOperationSuccess("User retrieved successfully.",
            transaction(database) {
                val result = Users.selectAll().where { Users.id eq user.userId }
                if (result.count() == 0L) {
                    null
                } else {
                    StickfixUser.from(result.single())
                }
            })

    /**
     * Adds a new user to the database.
     *
     * @param user A `ReadUser` instance containing the user information to be added to the database.
     */
    fun addUser(user: ReadUser): DatabaseOperationResult<StickfixUser> = transaction(database) {
        Users.insert {
            it[chatId] = user.userId  // Assigns the user's ID to the chatId column.
            it[username] = user.username  // Sets the username field.
            it[state] = IdleState::class.simpleName!!  // Initializes the state to 'Idle'.
        }
        DatabaseOperationSuccess("User added successfully.", StickfixUser(user.username, user.userId))
    }

    fun deleteUser(user: ReadUser) {
        transaction(database) {
            Users.deleteWhere { Users.id eq user.userId }
        }
    }

    fun setPrivateMode(user: ReadUser, mode: PrivateMode): DatabaseOperationResult<PrivateMode> {
        transaction(database) {
            Users.update({ Users.id eq user.userId }) {
                it[privateMode] = when (mode) {
                    PrivateMode.ENABLED -> true
                    PrivateMode.DISABLED -> false
                }
            }
        }
        return DatabaseOperationSuccess("Private mode set to $mode.", mode)
    }

    /**
     * Provides a string representation of the StickfixDatabase instance.
     *
     * @return String A string representation of the StickfixDatabase instance, including the JDBC URL and driver name.
     */
    override fun toString() =
        "StickfixDatabase(jdbcUrl='$jdbcUrl', driverName='$driverName')"
}
