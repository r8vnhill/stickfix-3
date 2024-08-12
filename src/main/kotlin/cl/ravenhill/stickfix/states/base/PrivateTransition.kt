package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `PrivateTransition` interface defines the contract for handling private mode transitions within StickFix.
 * Implementing classes are expected to manage the logic for enabling, disabling, and handling private mode transitions
 * for a user.
 */
interface PrivateTransition {

    /**
     * Handles the transition to private mode for the current state of the user. This method is responsible for managing
     * the logic associated with enabling or processing private mode, as defined by the implementing class.
     *
     * @return `TransitionResult` The result of the transition attempt, indicating success or failure.
     */
    fun onPrivateMode(): TransitionResult

    /**
     * Enables private mode for the user. This method manages the transition logic required to enable private mode
     * and updates the user's state accordingly.
     *
     * @return `TransitionResult` The result of the transition to enable private mode, indicating success or failure.
     */
    fun onPrivateModeEnabled(): TransitionResult

    /**
     * Disables private mode for the user. This method manages the transition logic required to disable private mode
     * and updates the user's state accordingly.
     *
     * @return `TransitionResult` The result of the transition to disable private mode, indicating success or failure.
     */
    fun onPrivateModeDisabled(): TransitionResult
}

/**
 * The `PrivateTransitionImpl` class provides a concrete implementation of the `PrivateTransition` interface.
 * It extends `AbstractState`, allowing it to utilize shared state-related functionality while implementing the
 * logic required to handle private mode transitions, including enabling and disabling private mode.
 *
 * @property user The `StickfixUser` instance representing the user associated with this state.
 */
class PrivateTransitionImpl(user: StickfixUser) : AbstractState(user), PrivateTransition {

    /**
     * Enables private mode for the current user in StickFix. This function is intended to be overridden by specific
     * states that support private mode. By default, it logs a transition failure message indicating that enabling
     * private mode is not supported in the current state and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `TransitionResult` indicating the failure to enable private mode in the current state.
     */
    context(StickfixBot)
    override fun onPrivateModeEnabled(): TransitionResult {
        logTransitionFailure("enable private mode")
        return TransitionFailure(this)
    }

    /**
     * Disables private mode for the current user in StickFix. This function is intended to be overridden by specific
     * states that support disabling private mode. By default, it logs a transition failure message indicating that
     * disabling private mode is not supported in the current state and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `TransitionResult` indicating the failure to disable private mode in the current state.
     */
    context(StickfixBot)
    override fun onPrivateModeDisabled(): TransitionResult {
        logTransitionFailure("disable private mode")
        return TransitionFailure(this)
    }

    /**
     * Handles the transition to private mode in StickFix. This function is intended to be overridden by specific states
     * that support private mode. By default, it logs a transition failure message indicating that transitioning to
     * private mode is not supported in the current state and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to manage the transition and interact with the Telegram API.
     * @return `TransitionResult` indicating the failure to transition to private mode in the current state.
     */
    context(StickfixBot)
    override fun onPrivateMode(): TransitionResult {
        logTransitionFailure("enable private mode")
        return TransitionFailure(this)
    }
}
