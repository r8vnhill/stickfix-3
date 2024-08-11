/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logDebug
import cl.ravenhill.stickfix.logError

/**
 * Represents the idle state for a user in the Stickfix bot application. This state indicates that the user is currently
 * not engaged in any specific action. The `IdleState` class implements the `State` interface, allowing for state
 * transitions such as starting a new action or revoking the bot.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class IdleState(override val user: StickfixUser) : State() {

    /**
     * Handles the transition from idle state to start state when the user initiates a start action. Updates the user's
     * state to `StartState` and returns a successful transition result.
     *
     * @return TransitionResult The result of the transition to `StartState`, indicating success.
     */
    context(StickfixBot)
    override fun onStart(): TransitionResult = tempDatabase.setUserState(user, ::StartState).fold(
        ifLeft = {
            logError(logger) { "Failed to update user state during start: $it" }
            TransitionFailure(this)
        },
        ifRight = {
            logDebug(logger) { "User ${user.debugInfo} is starting the registration process" }
            TransitionSuccess(it.data)
        }
    )

    /**
     * Handles the transition from idle state to revoke state when the user initiates a revocation action. Updates the
     * user's state to `RevokeState` and returns a successful transition result.
     *
     * @return TransitionResult The result of the transition to `RevokeState`, indicating success.
     */
    context(StickfixBot)
    override fun onRevoke(): TransitionResult = transitionToNewState(::RevokeState, "revoking registration")

    /**
     * Handles the transition from idle state to private mode state when the user initiates a private mode action. Updates
     * the user's state to `PrivateModeState` and returns a successful transition result.
     *
     * @return TransitionResult The result of the transition to `PrivateModeState`, indicating success.
     */
    context(StickfixBot)
    override fun onPrivateMode(): TransitionResult =
        transitionToNewState(::PrivateModeState, "enabling private mode")

    /**
     * Handles the transition from idle state to shuffle state when the user initiates a shuffle action. Updates the user's
     * state to `ShuffleState` and returns a successful transition result.
     *
     * @return TransitionResult The result of the transition to `ShuffleState`, indicating success.
     */
    context(StickfixBot)
    override fun onShuffle(): TransitionResult = transitionToNewState(::ShuffleState, "shuffling")

    /**
     * Handles the transition from idle state to a new state when the user initiates an action. Updates the user's
     * state to the specified `newState` and returns a successful transition result.
     *
     * @param newState The function that creates the new state.
     * @param actionDescription A description of the action being performed, used for logging purposes.
     * @return TransitionResult The result of the transition, indicating success or failure.
     */
    context(StickfixBot)
    private fun transitionToNewState(
        newState: (StickfixUser) -> State,
        actionDescription: String,
    ): TransitionResult {
        return databaseService.setUserState(user, newState).fold(
            ifLeft = {
                logError(logger) { "Failed to update user state during $actionDescription: $it" }
                TransitionFailure(this)
            },
            ifRight = {
                logDebug(logger) { "User ${user.debugInfo} is $actionDescription" }
                TransitionSuccess(it.data)
            }
        )
    }
}
