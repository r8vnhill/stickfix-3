/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.State
import cl.ravenhill.stickfix.states.TransitionResult
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
        /**
         * Creates a `StickfixUser` instance from a given `TelegramUser`. This function extracts the necessary
         * information from the `TelegramUser` and uses it to create a new `StickfixUser`.
         *
         * @param from The `TelegramUser` instance from which to create the `StickfixUser`.
         * @return A new `StickfixUser` instance with the username and ID extracted from the `TelegramUser`.
         */
        fun from(from: TelegramUser) = StickfixUser(from.username ?: "unknown", from.id)
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
    fun onIdle(bot: StickfixBot) = state.onIdle(bot)

    /**
     * Handles the revocation process for the user, updating the user's state in the database.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the revocation process, indicating success or failure.
     */
    fun onRevoke(bot: StickfixBot) = state.onRevoke(bot)

    /**
     * Handles the rejection of an action within the current state by delegating the call to the state's `onRejection`
     * method. This function ensures that the rejection logic defined in the current state is executed.
     *
     * @param bot The `StickfixBot` instance used to interact with the bot's functionalities.
     * @return `TransitionResult` indicating the result of the rejection handling, as determined by the current state's
     *   `onRejection` method.
     */
    fun onRejection(bot: StickfixBot): TransitionResult = state.onRejection(bot)
}
