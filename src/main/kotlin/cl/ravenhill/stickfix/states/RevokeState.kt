package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import org.slf4j.LoggerFactory

/**
 * Represents the state where a user can confirm or deny the revocation of their registration in StickFix.
 * This state allows the user to finalize their decision regarding the revocation process and handles the appropriate
 * transitions based on the user's input. The `RevokeState` class implements the `State` interface, facilitating
 * state-specific actions and transitions.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class RevokeState(override val user: StickfixUser) : SealedState(user) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the confirmation of the revocation process. When the user confirms the revocation, this method deletes
     * the user's data from the database, logs the operation, and sends a confirmation message to the user. If the
     * deletion is successful, it transitions the user to the `IdleState`. If the deletion fails, it logs the error and
     * returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's data.
     * @return `TransitionResult` indicating the outcome of the revocation confirmation process. This can be a
     *   `TransitionSuccess` if the user was successfully deleted and transitioned to the `IdleState`, or a
     *   `TransitionFailure` if the deletion failed.
     */
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

    /**
     * Handles the rejection of the revocation process. When the user rejects the revocation, this method logs the
     * rejection, sends a cancellation message to the user, and transitions the user back to the `IdleState`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's data.
     * @return `TransitionResult` indicating the success of canceling the revocation and transitioning the user back
     *   to the `IdleState`.
     */
    context(StickfixBot) override fun onRevokeRejection(): TransitionResult {
        logInfo(logger) { "User ${user.debugInfo} rejected revocation." }
        sendMessage(user, "Revocation process canceled.")
        databaseService.setUserState(user, ::IdleState)
        return TransitionSuccess(user.state)
    }
}
