/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.PrivateMode
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Represents a state in a state-driven application, providing a common interface for handling state-specific actions
 * and transitions. As a sealed interface, `State` ensures that all potential states are known at compile time, allowing
 * for exhaustive checking of state types. Each state encapsulates its own behavior and context, facilitating a robust
 * and scalable state management system.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows
 *   the state to have direct access to and modify user data as necessary during state transitions.
 */
sealed class State {
    abstract val user: StickfixUser

    /**
     * A logger instance for logging state-related actions. This logger is private to the state and is used to record
     * activities such as transitions and errors.
     */
    protected val logger: Logger get() = LoggerFactory.getLogger(javaClass)

    /**
     * Logs an error message for unauthorized or invalid state transitions.
     *
     * @param action The action that was attempted, leading to the transition failure.
     */
    private fun logTransitionFailure(action: String) {
        logError(logger) { "User ${user.debugInfo} attempted to $action from state ${javaClass.simpleName}" }
    }

    /**
     * Handles the start of an interaction within this state. By default, this method logs a warning
     * indicating an unauthorized or unexpected attempt to start from the current state and returns
     * a `TransitionFailure` with the current state as the next state, suggesting no transition
     * occurs.
     *
     * @return TransitionResult Indicates the failure to transition from this state, typically because the action
     *                          is not allowed or valid in the current context.
     */
    context(StickfixBot)
    open fun onStart(): TransitionResult {
        logTransitionFailure("start")
        return TransitionFailure(this)
    }

    /**
     * Transitions the user to the idle state. This function sets the user's state to `IdleState`, updates the state in
     * the database, and returns a `TransitionSuccess` indicating that the transition to the idle state was successful.
     *
     * @receiver The `StickfixBot` instance used to interact with the bot's functionalities and manage the database
     *   service.
     * @return `TransitionResult` indicating the success of the transition to the idle state.
     */
    context(StickfixBot)
    open fun onIdle(): TransitionResult {
        databaseService.setUserState(user, ::IdleState)
        return TransitionSuccess(user.state)
    }

    /**
     * Handles the revocation process in the current state.
     *
     * @return TransitionResult Indicates the failure to transition from the current state during revocation.
     */
    context(StickfixBot)
    open fun onRevoke(): TransitionResult {
        logTransitionFailure("revoke")
        return TransitionFailure(this)
    }

    /**
     * Handles the rejection of the start command within the current state by logging an error message and returning a
     * `TransitionFailure`. This function is called when a user attempts to reject the start command, indicating that
     * the transition to a state representing the rejection of the start command has failed.
     *
     * @return `TransitionResult` indicating the failure to transition to a state representing the rejection of the
     *   start command.
     */
    context(StickfixBot)
    open fun onStartRejection(): TransitionResult {
        logTransitionFailure("reject start")
        return TransitionFailure(this@State)
    }

    /**
     * Handles the confirmation of the start action for the current state of the user. This function logs an error
     * message indicating that the user attempted to confirm the start action from an invalid state and returns a
     * `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult The result of the start confirmation transition, indicating failure due to an invalid
     *   state.
     */
    context(StickfixBot)
    open fun onStartConfirmation(): TransitionResult {
        logTransitionFailure("confirm start")
        return TransitionFailure(this@State)
    }

    /**
     * Handles the confirmation of the revoke action within the current state by logging an error message and returning a
     * `TransitionFailure`. This function is called when a user attempts to confirm the revoke action, indicating that
     * the transition to a state representing the revocation has failed.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `TransitionResult` indicating the failure to transition to a state representing the revoke action.
     */
    context(StickfixBot)
    open fun onRevokeConfirmation(): TransitionResult {
        logTransitionFailure("revoke")
        return TransitionFailure(this)
    }

    /**
     * Enables private mode for the current user in the Stickfix bot application. This function updates the user's
     * private mode setting in the database to `PrivateMode.ENABLED` and logs the operation. If the database update is
     * successful, the function returns a `TransitionSuccess` result indicating that the transition to private mode was
     * successful. If the update fails, it logs the error and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `TransitionResult` indicating the outcome of the operation to enable private mode. The result is either a
     *   `TransitionSuccess` if the private mode was enabled successfully, or a `TransitionFailure` if the operation
     *   failed.
     */
    context(StickfixBot)
    open fun onPrivateModeEnabled(): TransitionResult {
        logTransitionFailure("enable private mode")
        return TransitionFailure(this)
    }

    /**
     * Disables private mode for the current user in the Stickfix bot application. This function updates the user's
     * private mode setting in the database to `PrivateMode.DISABLED` and logs the operation. If the database update is
     * successful, the function returns a `TransitionSuccess` result indicating that the transition to disable private
     * mode was successful. If the update fails, it logs the error and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `TransitionResult` indicating the outcome of the operation to disable private mode. The result is either
     *   a `TransitionSuccess` if the private mode was disabled successfully, or a `TransitionFailure` if the operation
     *   failed.
     */
    context(StickfixBot)
    open fun onPrivateModeDisabled(): TransitionResult {
        logTransitionFailure("disable private mode")
        return TransitionFailure(this)
    }

    /**
     * Handles the transition to private mode in the Stickfix bot application. This function is intended to be
     * overridden by specific states that support private mode. By default, it logs a transition failure message
     * indicating that enabling private mode is not supported in the current state and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to manage the transition and interact with the Telegram API.
     * @return TransitionResult The result of the attempted transition, which by default is a `TransitionFailure`
     *   indicating that private mode cannot be enabled from the current state.
     */
    context(StickfixBot)
    open fun onPrivateMode(): TransitionResult {
        logTransitionFailure("enable private mode")
        return TransitionFailure(this)
    }
}

