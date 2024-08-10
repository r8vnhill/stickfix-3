package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo

/**
 * Represents the state where a user can confirm or deny the revocation of their registration in the Stickfix bot
 * application. This state allows the user to finalize their decision regarding the revocation process and handles the
 * appropriate transitions based on the user's input. The `RevokeState` class implements the `State` interface,
 * facilitating state-specific actions and transitions.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class RevokeState(override val user: StickfixUser) : State() {

    context(StickfixBot) override fun onRevokeConfirmation(): TransitionResult {
        logInfo(logger) { "User ${user.debugInfo} confirmed revocation." }
        return databaseService.deleteUser(user).fold(
            ifLeft = {
                logError(logger) { "Failed to delete user ${user.debugInfo}: $it" }
                TransitionFailure(this)
            },
            ifRight = {
                logInfo(logger) { "User ${user.debugInfo} deleted successfully." }
                sendMessage(user, "Your registration has been revoked.")
                TransitionSuccess(IdleState(user))
            }
        )
    }
}
