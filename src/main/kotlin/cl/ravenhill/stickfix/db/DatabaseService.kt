/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.chat.ReadUser
import org.jetbrains.exposed.sql.Database

/**
 * Defines the interface for interacting with a database, specifying the essential operations
 * necessary for initializing and managing a database connection. This interface serves as a
 * contract for implementing classes to ensure they provide core functionalities such as
 * initialization, authentication, and user management.
 *
 * ## Usage:
 * Implement this interface in classes responsible for database operations. The `init` method should
 * be invoked to configure and establish the database connection prior to executing any database
 * operations. Implementers can also manage user details and authentication through the `apiToken`.
 *
 * ### Example 1: Implementing DatabaseService
 * ```kotlin
 * class MyDatabaseService : DatabaseService {
 *     override var apiToken: String = ""
 *
 *     override fun init(): DatabaseService {
 *         // Initialization logic, possibly setting up connection parameters
 *         return this
 *     }
 *
 *     override fun getUser(user: ReadUser): ReadUser? {
 *         // Implementation to retrieve user details from the database
 *         return user
 *     }
 *
 *     override fun addUser(user: ReadUser) {
 *         // Implementation to add user details to the database
 *     }
 * }
 * ```
 *
 * @property apiToken
 *   The API token used for authenticating and authorizing interactions with external services or
 *   databases. This key is essential for operations that require secure access.
 * @property database
 *   The database instance used for executing queries and operations.
 */
interface DatabaseService {

    val database: Database

    var apiToken: String

    /**
     * Initializes the database service, setting up necessary components or connections for database
     * interactions.
     *
     * ## Usage:
     * Invoke this method on an instance of a class implementing `DatabaseService` to prepare the
     * service for operations. This setup may involve establishing database connections or
     * configuring authentication parameters.
     *
     * ### Example 1: Initializing a DatabaseService Implementation
     * ```kotlin
     * val myService = MyDatabaseService().init()
     * ```
     *
     * @return DatabaseService An instance of the service, often itself, to facilitate method chaining or further
     *   configurations.
     */
    fun init(): DatabaseService

    /**
     * Retrieves user details based on the provided user information.
     *
     * @param user
     *  A `ReadUser` instance containing minimal user information for which details are to be
     *  retrieved.
     * @return ReadUser?
     *  The user details if found, or null if no user matches the provided information.
     */
    fun getUser(user: ReadUser): ReadUser?

    /**
     * Adds a user to the database.
     *
     * @param user
     *  A `ReadUser` instance containing the user information to be added to the database.
     */
    fun addUser(user: ReadUser)

    companion object {

        /**
         * A constant identifier for the API token used for accessing and authenticating with
         * external services or databases. This constant aids in the uniform handling of API token
         * keys across the implementation.
         *
         * ## Usage:
         * Utilize this constant wherever the API token key is required for authentication or
         * authorization processes.
         *
         * ### Example 1: Using the API token key
         * ```kotlin
         * val apiKey = DatabaseService.API_TOKEN_KEY
         * myService.setApiKey(apiKey)
         * ```
         */
        const val API_TOKEN_KEY = "API_KEY"
    }
}

