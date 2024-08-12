package cl.ravenhill.stickfix.chat.handlers

import cl.ravenhill.stickfix.Stateful
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `PrivateModeHandler` interface defines methods for handling the private mode functionality within StickFix.
 * It extends the `Stateful` interface, ensuring that any implementing class is associated with a `State`.
 *
 * This interface provides functions to enable and disable private mode, with the actual logic being delegated to
 * the user's current state.
 */
interface PrivateModeHandler : Stateful {

    /**
     * Handles enabling private mode by delegating the process to the current state of the user. The function calls
     * the `onPrivateModeEnabled` method on the user's current state, which manages the transition and associated
     * actions for enabling private mode.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's state.
     * @return `TransitionResult` The result of the state transition, indicating the outcome of enabling private mode.
     *         The result can be a `TransitionSuccess` if the transition was successful, or a `TransitionFailure` if
     *         enabling private mode was not allowed from the current state.
     */
    context(StickfixBot)
    fun onPrivateModeEnabled(): TransitionResult = state.onPrivateModeEnabled()

    /**
     * Handles the transition to disable private mode by delegating the process to the user's current state. This function
     * calls the `onPrivateModeDisabled` method on the user's current state, allowing the state to manage the disabling
     * of private mode. The result of this operation is returned as a `TransitionResult`, which indicates whether the
     * transition was successful or not.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's state.
     * @return `TransitionResult` The outcome of the transition to disable private mode. This result can be either a
     *         `TransitionSuccess` if the transition was successful, or a `TransitionFailure` if the transition failed.
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

}
