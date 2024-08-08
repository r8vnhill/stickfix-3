/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import org.jetbrains.exposed.sql.Database

interface DatabaseService {

    val database: Database

    var apiToken: String

    fun init(): DatabaseService

    fun getUser(user: StickfixUser): DatabaseOperationResult<StickfixUser?>

    fun addUser(user: StickfixUser): DatabaseOperationResult<StickfixUser>

    fun deleteUser(user: StickfixUser): DatabaseOperationResult<StickfixUser>

    companion object {
        const val API_TOKEN_KEY = "API_KEY"
    }
}
