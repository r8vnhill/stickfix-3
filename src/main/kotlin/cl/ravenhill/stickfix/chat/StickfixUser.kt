/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.jakt.Jakt.constraints
import cl.ravenhill.jakt.constraints.BeNull
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.RevokeState
import cl.ravenhill.stickfix.states.StartState
import cl.ravenhill.stickfix.states.State
import cl.ravenhill.stickfix.states.TransitionResult
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.transaction
import com.github.kotlintelegrambot.entities.User as TelegramUser

/**
 * Represents a user in the StickFix application, containing essential information and state
 * management for interactions with the Telegram bot.
 *
 * @property username The username of the user, used primarily for identification and interaction purposes within the
 *   bot.
 * @property userId The unique identifier for the user, typically linked to their Telegram account.
 */
data class StickfixUser(
    val username: String,
    val userId: Long,
) {
    /**
     * The state of the user within the StickFix bot. This property manages the current condition or phase
     * of interaction the user is in, which can dictate the bot's responses and actions.
     */
    var state: State = IdleState(this)

    /**
     * Provides a concise string representation of the user, useful for logging and debugging purposes. This property
     * returns the username if available, or the user ID otherwise.
     *
     * @return String The debug information for the user.
     */
    val debugInfo: String get() = username.ifBlank { userId.toString() }

    companion object {
        fun from(from: TelegramUser) = StickfixUser(from.username ?: "unknown", from.id)

        fun from(row: ResultRow) = transaction {
            constraints {
                "User must have an ID" {
                    row.getOrNull(Users.id) mustNot BeNull
                }
                "User must have a username" {
                    row.getOrNull(Users.username) mustNot BeNull
                }
                "User must have a state" {
                    row.getOrNull(Users.state) mustNot BeNull
                }
            }
            val user = StickfixUser(row[Users.username], row[Users.chatId])
            user.state = when (row[Users.state]) {
                IdleState::class.simpleName -> IdleState(user)
                StartState::class.simpleName -> StartState(user)
                RevokeState::class.simpleName -> RevokeState(user)
                else -> throw IllegalArgumentException("Unknown state")
            }
            user
        }
    }

    /**
     * Handles the start of an interaction for the user, delegating the action to the current state.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition attempt, indicating success or failure.
     */
    fun onStart(bot: StickfixBot): TransitionResult = state.onStart(bot)

    /**
     * Handles the transition to the idle state for the user, updating the user's state in the database.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition to the idle state, indicating success or failure.
     */
    fun onIdle(bot: StickfixBot): TransitionResult {
        bot.databaseService.setUserState<IdleState>(userId)
        return state.onIdle(bot)
    }

    /**
     * Handles the revocation process for the user, updating the user's state in the database.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the revocation process, indicating success or failure.
     */
    fun onRevoke(bot: StickfixBot): TransitionResult {
        bot.databaseService.setUserState<RevokeState>(userId)
        return state.onRevoke(bot)
    }
}
