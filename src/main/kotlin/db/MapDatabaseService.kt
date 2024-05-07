/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package db

import db.DatabaseService.Companion.API_TOKEN_KEY

/**
 * Provides a simple implementation of the `DatabaseService` interface using an in-memory map to
 * simulate a database. This service is useful for testing or lightweight data management scenarios
 * where no actual database connection is required.
 *
 * ## Usage:
 * The `MapDatabaseService` should be instantiated and initialized before use. The `init` method, in
 * this case, does not perform any significant operations but returns the instance itself, allowing
 * for method chaining if additional setup methods were to be defined.
 *
 * ### Example 1: Creating and Initializing MapDatabaseService
 * ```kotlin
 * val databaseService: DatabaseService = MapDatabaseService().init()
 * ```
 *
 * @constructor Creates an instance of `MapDatabaseService`.
 */
class MapDatabaseService : DatabaseService {

    /**
     * A mutable map that simulates a database by storing key-value pairs.
     */
    private val _meta = mutableMapOf(
        API_TOKEN_KEY to ""
    )

    override val apiToken: String
        get() = _meta[API_TOKEN_KEY] ?: ""

    /**
     * A read-only view of the `_meta` map.
     */
    val meta: Map<String, String> = _meta

    /**
     * Initializes the database service. For `MapDatabaseService`, this method is a no-operation
     * method that simply returns the instance of this class, facilitating method chaining.
     *
     * @return Returns itself, allowing for any further configuration to be chained.
     */
    override fun init() = this

    /**
     * Sets the API key for simulated database operations. This key can be used to validate operations or
     * simulate scenarios where authentication is required.
     *
     * @param token The API key to be used, should be a non-null string.
     */
    override fun setApiToken(token: String) {
        _meta[API_TOKEN_KEY] = token
    }
}
