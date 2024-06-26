/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.State

/**
 * Represents a user in the StickFix application, containing essential information and state
 * management for interactions with the Telegram bot. This class implements the `ReadWriteUser`
 * interface, ensuring it provides functionalities for both reading and writing user data as needed
 * by the bot's operations.
 *
 * ## Usage:
 * This class is primarily used to manage user information and states during interactions with the
 * StickFix bot. Each instance of `StickfixUser` is associated with specific user commands and state
 * transitions.
 *
 * ### Example 1: Creating a StickfixUser
 * ```kotlin
 * val user = StickfixUser("user123", 123456789L)
 * println("User created with username: ${user.username} and userId: ${user.userId}")
 * ```
 *
 * @property username
 *  The username of the user, used primarily for identification and interaction purposes within the
 *  bot.
 * @property userId The unique identifier for the user, typically linked to their Telegram account.
 */
data class StickfixUser(
    override val username: String,
    override val userId: Long
) : ReadWriteUser {
    /**
     * The state of the user within the StickFix bot. This property manages the current condition or phase
     * of interaction the user is in, which can dictate the bot's responses and actions.
     */
    override var state: State = IdleState(this)
}
