/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.chat.ReadUser
import org.jetbrains.exposed.sql.Database

interface DatabaseService {

    val database: Database

    var apiToken: String

    fun init(): DatabaseService

    fun getUser(user: ReadUser): DatabaseOperationResult<ReadUser?>

    fun addUser(user: ReadUser): DatabaseOperationResult<ReadUser>

    fun deleteUser(user: ReadUser): DatabaseOperationResult<ReadUser>

    companion object {
        const val API_TOKEN_KEY = "API_KEY"
    }
}
