package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.PrivateMode
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo

/**
 * Represents the state where a user can enable or disable private mode in the Stickfix bot application. This state
 * allows the user to change their privacy settings and handles the appropriate transitions based on the user's input.
 * The `PrivateModeState` class implements the `State` interface, facilitating state-specific actions and transitions.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class PrivateModeState(override val user: StickfixUser) : State() {

    context(StickfixBot) override fun onPrivateModeEnabled(): TransitionResult {
        return databaseService.setPrivateMode(user, PrivateMode.ENABLED).fold(
            ifLeft = {
                logError(logger) { "Failed to enable private mode for user ${user.debugInfo}: $it" }
                TransitionFailure(this)
            },
            ifRight = {
                logInfo(logger) { "User ${user.username} enabled private mode." }
                TransitionSuccess(user.state)
            }
        )
    }

    context(StickfixBot)
    override fun onPrivateModeDisabled(): TransitionResult =
        databaseService.setPrivateMode(user, PrivateMode.DISABLED).fold(
            ifLeft = {
                logError(logger) { "Failed to disable private mode for user ${user.debugInfo}: $it" }
                TransitionFailure(this)
            },
            ifRight = {
                logInfo(logger) { "User ${user.username} disabled private mode." }
                TransitionSuccess(user.state)
            }
        )
}
