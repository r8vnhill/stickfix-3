/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logDebug
import cl.ravenhill.stickfix.logError
import org.slf4j.LoggerFactory

/**
 * Represents the idle state for a user in the Stickfix bot application. This state indicates that the user is currently
 * not engaged in any specific action. The `IdleState` class implements the `State` interface, allowing for state
 * transitions such as starting a new action or revoking the bot.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class IdleState(override val user: StickfixUser) : State {

    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    init {
        user.state = this
    }

    /**
     * Handles the transition from idle state to start state when the user initiates a start action. Updates the user's
     * state to `StartState` and returns a successful transition result.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition to `StartState`, indicating success.
     */
    context(StickfixBot)
    override fun onStart(): TransitionResult {
        databaseService.setUserState(StartState(user)).fold(
            ifLeft = { logError(logger) { "Failed to update user state: $it" } },
            ifRight = { logDebug(logger) { "User state updated to $it" } }
        )
        return TransitionSuccess(user.state)
    }

    /**
     * Handles the transition from idle state to revoke state when the user initiates a revocation action. Updates the
     * user's state to `RevokeState` and returns a successful transition result.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition to `RevokeState`, indicating success.
     */
    override fun onRevoke(bot: StickfixBot): TransitionResult {
        user.state = RevokeState(user)
        return TransitionSuccess(user.state)
    }

    /**
     * Handles the rejection of the start command while the user is in the idle state. This function logs a debug
     * message indicating the user's attempt to reject the start command from the idle state and returns a
     * `TransitionFailure`.
     *
     * @receiver The `StickfixBot` instance used to interact with the bot's functionalities and manage the database
     *   service.
     * @return `TransitionResult` indicating the failure to transition to a state representing the rejection of the
     *   start command from the idle state.
     */
    context(StickfixBot)
    override fun onStartRejection(): TransitionResult {
        logDebug(logger) { "User ${user.debugInfo} attempted to reject from idle state" }
        return TransitionFailure(this)
    }
}
