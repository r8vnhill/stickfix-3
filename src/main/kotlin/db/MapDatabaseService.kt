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
 * @property apiToken The API token used for authentication.
 */
class MapDatabaseService : DatabaseService {

    /**
     * A mutable map that simulates a database by storing key-value pairs.
     */
    private val _meta = mutableMapOf(
        API_TOKEN_KEY to ""
    )

    override var apiToken: String
        get() = _meta[API_TOKEN_KEY] ?: ""
        set(value) {
            _meta[API_TOKEN_KEY] = value
        }

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
}
