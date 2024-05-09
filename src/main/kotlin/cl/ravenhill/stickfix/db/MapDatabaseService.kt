/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService.Companion.API_TOKEN_KEY

/**
 * Implements the `DatabaseService` interface using an in-memory map to simulate a database. This
 * service is particularly useful for testing or lightweight data management scenarios where an
 * actual database connection is not necessary. It provides a simulated environment for operations
 * typically performed against a database, such as storing and retrieving user data and managing an
 * API token.
 *
 * ## Usage:
 * Instantiate `MapDatabaseService`, initialize it, and use its methods to simulate database
 * operations. The `init` method in this implementation does not perform any significant operations
 * but returns the instance itself, allowing for method chaining with other setup or operational
 * methods.
 *
 * ### Example 1: Creating and Initializing MapDatabaseService
 * ```kotlin
 * val databaseService: DatabaseService = MapDatabaseService().init()
 * databaseService.apiToken = "your_api_token_here"
 * val user = SimpleUser("username", 1L) // Assuming SimpleUser is a concrete implementation of ReadUser
 * println(databaseService.getUser(user)?.username)
 * ```
 *
 * @constructor Creates an instance of `MapDatabaseService`.
 * @property apiToken
 *  The API token used for authentication. This token is stored and retrieved from the internal
 *  `_meta` map, simulating the behavior of secure token storage.
 */
class MapDatabaseService : DatabaseService {

    /**
     * A mutable map that simulates a database by storing key-value pairs. Initially used to store
     * API tokens and can be extended to store other types of metadata.
     */
    private val _meta = mutableMapOf<String, String>(
        API_TOKEN_KEY to ""
    )

    /**
     * A mutable map that simulates a user database by storing `ReadUser` instances against their
     * unique IDs.
     */
    private val _users = mutableMapOf<Long, ReadUser>()

    override var apiToken: String
        get() = _meta[API_TOKEN_KEY] ?: ""
        set(value) {
            _meta[API_TOKEN_KEY] = value
        }

    /**
     * Provides a read-only view of the `_meta` map, allowing external access without modifying the
     * map. This is useful for debugging or inspection purposes.
     */
    val meta: Map<String, String> = _meta.toMap()

    /**
     * Initializes the database service. This method is primarily a no-operation method in this
     * implementation but returns the instance of this class to facilitate method chaining.
     *
     * @return `MapDatabaseService` itself, allowing for method chaining and further configuration.
     */
    override fun init() = this

    /**
     * Retrieves a `ReadUser` instance by their user ID from the simulated user database. If no user
     * matches the provided user ID, this method returns `null`.
     *
     * @param user A `ReadUser` instance providing the ID for the lookup.
     * @return `ReadUser?` The user associated with the given ID, or `null` if no such user exists.
     */
    override fun getUser(user: ReadUser) = _users[user.userId]
}
