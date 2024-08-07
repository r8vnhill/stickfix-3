/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.RevokeState
import cl.ravenhill.stickfix.states.State
import cl.ravenhill.stickfix.states.TransitionResult
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Represents a read-only view of a user in the Stickfix bot application. This interface defines the essential
 * properties and methods that any user class must implement, ensuring consistent interaction with user data and state
 * transitions.
 *
 * @property username The username of the user. This provides read-only access to the user's username.
 * @property userId The unique identifier of the user. This provides read-only access to the user's ID.
 * @property state The current state of the user. This provides read-only access to the user's state.
 */
interface ReadUser {
    val username: String
    val userId: Long
    val state: State

    /**
     * Handles the start of an interaction for the user, delegating the action to the current state.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition attempt, indicating success or failure.
     */
    fun onStart(bot: StickfixBot): TransitionResult = state.onStart(bot)

    /**
     * Provides a concise string representation of the user, useful for logging and debugging purposes. This property
     * returns the username if available, or the user ID otherwise.
     *
     * @return String The debug information for the user.
     */
    val debugInfo: String get() = username.ifBlank { userId.toString() }

    /**
     * Handles the transition to the idle state for the user, updating the user's state in the database.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition to the idle state, indicating success or failure.
     */
    fun onIdle(bot: StickfixBot): TransitionResult {
        transaction {
            Users.update({ Users.id eq userId }) {
                it[state] = IdleState::class.simpleName!!
            }
        }
        return state.onIdle(bot)
    }

    /**
     * Handles the revocation process for the user, updating the user's state in the database.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the revocation process, indicating success or failure.
     */
    fun onRevoke(bot: StickfixBot): TransitionResult {
        transaction {
            Users.update({ Users.id eq userId }) {
                it[state] = RevokeState::class.simpleName!!
            }
        }
        return state.onRevoke(bot)
    }
}
