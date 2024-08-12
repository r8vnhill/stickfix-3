/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.handlers.StartHandler
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.State
import cl.ravenhill.stickfix.states.TransitionResult
import com.github.kotlintelegrambot.entities.User as TelegramUser

/**
 * Represents a user in the StickFix application, containing essential information and state management for interactions
 * with the Telegram bot.
 *
 * @property username The username of the user, used primarily for identification and interaction purposes within the
 *   bot.
 * @property id The unique identifier for the user, typically linked to their Telegram account.
 */
data class StickfixUser(
    val username: String,
    override val id: Long,
) : StickfixChat, StartHandler {
    /**
     * The state of the user within the StickFix bot. This property manages the current condition or phase
     * of interaction the user is in, which can dictate the bot's responses and actions.
     */
    override var state: State = IdleState(this)

    /**
     * Provides a concise string representation of the user, useful for logging and debugging purposes. This property
     * returns the username if available, or the user ID otherwise.
     *
     * @return String The debug information for the user.
     */
    override val debugInfo: String get() = if (username.isNotBlank()) "'$username'" else id.toString()

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

    /**
     * Handles the enabling of private mode by delegating the process to the current state of the user. This function
     * calls the `onPrivateModeEnabled` method on the user's current state to handle the transition and actions
     * associated with enabling private mode.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's state.
     * @return TransitionResult The result of the state transition, indicating the outcome of enabling private mode.
     *   It can be a success if the transition was valid, or a failure if enabling private mode was not allowed from the
     *   current state.
     */
    context(StickfixBot)
    fun onPrivateModeEnabled(): TransitionResult = state.onPrivateModeEnabled()

    /**
     * Handles the transition to disable private mode for the current state. This function delegates the disabling of
     * private mode to the current state of the user, allowing the state to manage how private mode is disabled. The
     * result of this operation is returned as a `TransitionResult`, which indicates whether the transition was
     * successful or not.
     *
     * @return `TransitionResult` indicating the outcome of the transition to disable private mode. This result can be
     *   either a `TransitionSuccess` if the transition was successful, or a `TransitionFailure` if the transition failed.
     */
    context(StickfixBot)
    fun onPrivateModeDisabled(): TransitionResult = state.onPrivateModeDisabled()

    /**
     * Handles the transition to private mode for the current state of the user in the Stickfix bot application.
     * This function delegates the responsibility of processing the private mode transition to the user's current state.
     *
     * @receiver StickfixBot The bot instance used to interact with the user's state and manage the private mode transition.
     * @return TransitionResult The result of the private mode transition, as determined by the user's current state.
     * This can be either a `TransitionSuccess` indicating a successful transition, or a `TransitionFailure` if the
     * transition was not successful.
     */
    context(StickfixBot)
    fun onPrivateMode(): TransitionResult = state.onPrivateMode()

    /**
     * Handles the shuffle action for the current state of the user in the Stickfix bot application. This method
     * delegates the shuffle action to the current state of the user by invoking the `onShuffle` method of that state.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the state transitions.
     * @return TransitionResult The result of the shuffle action, which can either be a success, indicating a successful
     *   transition to shuffle mode, or a failure if the transition was not possible.
     */
    context(StickfixBot)
    fun onShuffle(): TransitionResult = state.onShuffle()

    /**
     * Initiates the process to enable shuffle mode for the current state of the user. This function delegates the
     * operation to the state's `onShuffleEnabled` method, allowing the state to handle the transition to shuffle mode
     * according to its specific logic.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the state transitions.
     * @return TransitionResult The result of the shuffle mode enabling process, indicating success or failure.
     */
    context(StickfixBot)
    fun onShuffleEnabled(): TransitionResult = state.onShuffleEnabled()

    /**
     * Initiates the process to disable shuffle mode for the current state of the user. This function delegates the
     * operation to the state's `onShuffleDisabled` method, allowing the state to handle the transition to disable shuffle
     * mode according to its specific logic.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the state transitions.
     * @return TransitionResult The result of the shuffle mode disabling process, indicating success or failure.
     */
    context(StickfixBot)
    fun onShuffleDisabled(): TransitionResult = state.onShuffleDisabled()

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
