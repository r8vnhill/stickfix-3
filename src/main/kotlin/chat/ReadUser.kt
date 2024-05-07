/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package chat

/**
 * Defines a minimal set of user information for reading purposes within applications. This interface
 * is typically implemented by classes that need to represent basic user identity attributes such as
 * username and user ID.
 *
 * @property username
 *  A string representing the user's username. This is a read-only property.
 * @property userId
 *  A long representing the unique identifier for the user. This is a read-only property.
 */
interface ReadUser {
    val username: String
    val userId: Long
}
