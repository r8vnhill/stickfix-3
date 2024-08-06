/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.schema.Meta
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * A concrete implementation of the `DatabaseService` that manages a database connection
 * using JDBC. This class is responsible for initializing the database, managing API tokens,
 * and performing database transactions.
 *
 * ## Usage:
 * Instantiate `StickfixDatabase` with a JDBC URL and a driver name. Use the `init` method
 * to establish a connection and create necessary tables. API tokens can be set or retrieved
 * using the provided methods.
 *
 * @constructor Creates a new instance of `StickfixDatabase` with specified JDBC URL and driver
 *  name.
 * @param jdbcUrl The JDBC URL for the database connection.
 * @param driverName The fully qualified name of the JDBC driver.
 * @property database Holds the `Database` instance once `init` is called. This property is late-initialized.
 * @property apiToken Provides a getter to retrieve the API token from the `Meta` table in the database.
 */
class StickfixDatabase(private val jdbcUrl: String, private val driverName: String) :
    DatabaseService {

    override lateinit var database: Database
        private set

    /**
     * Initializes the database connection using the provided JDBC URL and driver name.
     * This method also ensures that the `Meta` table is created if it does not already exist.
     *
     * @return DatabaseService Returns this instance, allowing for method chaining.
     */
    override fun init(): DatabaseService {
        database = Database.connect(jdbcUrl, driverName)
        transaction(database) {
            SchemaUtils.create(Meta)
        }
        return this
    }

    /**
     * Retrieves the current API token stored in the database. This method executes a
     * database transaction to fetch the token from the `Meta` table.
     *
     * @return String The current API token.
     */
    override var apiToken: String
        get() = transaction(database) {
            Meta.selectAll().single()[Meta.key]
        }
        set(value) {
            transaction(database) {
                Meta.insert { row ->
                    row[key] = "API_KEY"
                    row[Meta.value] = value
                }
            }
        }

    override fun getUser(user: ReadUser): ReadUser? {
        TODO()
    }

    override fun addUser(user: ReadUser) {
        TODO("Not yet implemented")
    }

    override fun toString() =
        "StickfixDatabase(jdbcUrl='$jdbcUrl', driverName='$driverName')"

    override fun equals(other: Any?): Boolean = when (other) {
        is StickfixDatabase -> jdbcUrl == other.jdbcUrl && driverName == other.driverName
        else                -> false
    }

    override fun hashCode(): Int = Objects.hash(StickfixDatabase::class, jdbcUrl, driverName)
}
