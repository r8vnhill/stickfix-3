package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import org.slf4j.LoggerFactory


/**
 * Represents the state where a user can confirm or deny their registration in the Stickfix bot application. This state
 * allows the user to finalize their decision regarding the registration process and handles the appropriate transitions
 * based on the user's input. The `StartState` class implements the `State` interface, facilitating state-specific
 * actions and transitions.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class StartState(override val user: StickfixUser) : SealedState(user) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the rejection of the start action by the user. This function logs an informational message indicating
     * that the user chose not to register, updates the user's state to `StartRejectionState`, and returns a
     * `TransitionSuccess` result.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult The result of the start rejection transition, indicating success.
     */
    context(StickfixBot)
    override fun onStartRejection(): TransitionResult {
        logInfo(logger) { "User ${user.debugInfo} chose not to register." }
        tempDatabase.deleteUser(user).fold(
            ifLeft = {
                logError(logger) { "Failed to delete user ${user.debugInfo}: $it" }
            },
            ifRight = {
                logInfo(logger) { "User ${user.debugInfo} deleted successfully." }
            }
        )
        return TransitionSuccess(user.state)
    }

    /**
     * Handles the user's confirmation of the start action. This function logs the confirmation, attempts to add the
     * user to the database, and updates the user's state accordingly.
     *
     * If the user is successfully added to the database, their state is transitioned to `IdleState`. If the addition
     * fails, the function logs an error and returns a `TransitionFailure` result.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return TransitionResult The result of the start confirmation transition, indicating success or failure.
     */
    context(StickfixBot)
    override fun onStartConfirmation(): TransitionResult {
        logInfo(logger) { "User ${user.debugInfo} confirmed registration." }
        return databaseService.addUser(user).fold(
            ifLeft = {
                logError(logger) { "Failed to register user: $it" }
                TransitionFailure(this)
            },
            ifRight = {
                databaseService.setUserState(user, ::IdleState)
                tempDatabase.deleteUser(user).fold(
                    ifLeft = {
                        logError(logger) { "Failed to delete user ${user.debugInfo}: $it" }
                    },
                    ifRight = {
                        logInfo(logger) { "User ${user.debugInfo} deleted successfully." }
                    }
                )
                TransitionSuccess(user.state)
            }
        )
    }

    /**
     * Provides a string representation of the state, returning the simple name of the class.
     *
     * @return String The simple name of the class.
     */
    override fun toString(): String = javaClass.simpleName
}
