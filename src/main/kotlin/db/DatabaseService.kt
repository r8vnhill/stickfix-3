/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package db

/**
 * Represents a service for interacting with a database. This interface defines the basic operations
 * for initializing a connection or setup necessary for the database interactions.
 *
 * ## Usage:
 * This interface is typically implemented by classes that handle database operations. Once
 * implemented, the [init] method should be called to set up or initialize the database before
 * performing any operations.
 *
 * ### Example 1: Implementing db.DatabaseService
 * ```kotlin
 * class MyDatabaseService : db.DatabaseService {
 *     private var apiKey: String = ""
 *
 *     override fun init(): db.DatabaseService {
 *         // Initialization logic here
 *         return this
 *     }
 *
 *     override fun setApiKey(apiKey: String) {
 *         this.apiKey = apiKey
 *     }
 * }
 * ```
 *
 * @property apiToken
 *  The API key used for accessing the database. This key may be required for authentication or
 *  authorization purposes when interacting with external services or databases.
 */
interface DatabaseService {

    val apiToken: String

    /**
     * Initializes the database service. This function should set up the necessary components or
     * connections required to interact with the database.
     *
     * ## Usage:
     * This function is meant to be called on an instance of a class implementing the
     * `DatabaseService` interface. It prepares the service for operation, such as establishing
     * database connections or setting up required parameters and configurations.
     *
     * ### Example 1: Initializing a DatabaseService Implementation
     * ```kotlin
     * val myService = MyDatabaseService().init()
     * ```
     *
     * @return
     *  DatabaseService Returns the instance of the service, facilitating method chaining or
     *  testing scenarios.
     */
    fun init(): DatabaseService

    /**
     * Sets the API key for the database service. This API key may be required for authentication
     * and authorization purposes when interacting with external services or databases.
     *
     * @param token
     *  The API key to be used for accessing the database. This should be a valid, non-null string
     *  representing the key provided by the database service.
     */
    fun setApiToken(token: String)

    companion object {

        /**
         * Constant variable that represents the API token key.
         *
         * This variable is used to identify and authenticate the API key
         * required for accessing external services or databases.
         *
         * ## Usage:
         * This constant should be used wherever the API token key is needed
         * for authentication or authorization purposes.
         *
         * ### Example 1: Setting up the API token key
         * ```kotlin
         * val apiKey = API_TOKEN_KEY
         * ```
         */
        const val API_TOKEN_KEY = "API_KEY"
    }
}
