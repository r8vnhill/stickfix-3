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
 * @property id The unique identifier for the user, typically linked to their Telegram account.
 */
data class StickfixUser(
    val username: String,
    val id: Long,
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
    val debugInfo: String get() = if (username.isNotBlank()) "'$username'" else id.toString()

    /**
     * Handles the start of an interaction for the user, delegating the action to the current state.
     *
     * @return TransitionResult The result of the transition attempt, indicating success or failure.
     */
    context(StickfixBot)
    fun onStart(): TransitionResult = state.onStart()

    /**
     * Transitions the user to the idle state by delegating the call to the current state's `onIdle` method. This
     * function ensures that the transition logic defined in the current state is executed.
     *
     * @receiver The `StickfixBot` instance used to interact with the bot's functionalities and manage the database
     *   service.
     * @return `TransitionResult` indicating the result of the transition to the idle state, as determined by the
     *   current state's `onIdle` method.
     */
    context(StickfixBot)
    fun onIdle() = state.onIdle()


    /**
     * Handles the revocation process for the user, updating the user's state in the database.
     *
     * @return TransitionResult The result of the revocation process, indicating success or failure.
     */
    context(StickfixBot)
    fun onRevoke() = state.onRevoke()

    /**
     * Handles the rejection of an action within the current state by delegating the call to the state's `onRejection`
     * method. This function ensures that the rejection logic defined in the current state is executed.
     *
     * @return `TransitionResult` indicating the result of the rejection handling, as determined by the current state's
     *   `onRejection` method.
     */
    context(StickfixBot)
    fun onStartRejection(): TransitionResult = state.onStartRejection()

    /**
     * Handles the confirmation of the start action for the current state of the user. This function delegates the
     * handling of the start confirmation to the current state of the user.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult The result of the start confirmation transition, indicating success or failure.
     */
    context(StickfixBot)
    fun onStartConfirmation(): TransitionResult = state.onStartConfirmation()

    /**
     * Facilitates the confirmation of a user's revoke action by delegating the process to the current state of the user.
     * This function calls the `onRevokeConfirmation` method on the user's current state to handle the confirmation of
     * revocation.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's state.
     * @return TransitionResult The result of the state transition, indicating the outcome of the revoke confirmation
     *   process. It can be a success if the transition was valid, or a failure if the revocation confirmation was not
     *   allowed from the current state.
     */
    context(StickfixBot)
    fun onRevokeConfirmation(): TransitionResult = state.onRevokeConfirmation()

    override fun toString() = "StickfixUser(username='$username', id=$id, state=${state::class.simpleName})"

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
}
