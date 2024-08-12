package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.modes.PrivateMode
import org.slf4j.LoggerFactory

/**
 * Represents the state where a user can enable or disable private mode in the Stickfix bot application. This state
 * allows the user to change their privacy settings and handles the appropriate transitions based on the user's input.
 * The `PrivateModeState` class implements the `State` interface, facilitating state-specific actions and transitions.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class PrivateModeState(override val user: StickfixUser) : SealedState(user) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the transition to enable private mode for the user. This function uses the
     * `handlePrivateModeTransition` method to perform the necessary database operation and log the result.
     * If the operation is successful, it logs a success message and returns a `TransitionSuccess` result.
     * Otherwise, it logs a failure message and returns a `TransitionFailure` result.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return TransitionResult The result of the private mode transition, indicating success or failure.
     */
    context(StickfixBot)
    override fun onPrivateModeEnabled(): TransitionResult =
        handlePrivateModeTransition(
            mode = PrivateMode.ENABLED,
            successMessage = "User enabled private mode.",
            failureMessage = "Failed to enable private mode for user"
        )

    /**
     * Handles the transition to disable private mode for the user. This function uses the `handlePrivateModeTransition`
     * method to perform the necessary database operation and log the result. If the operation is successful, it logs a
     * success message and returns a `TransitionSuccess` result. Otherwise, it logs a failure message and returns a
     * `TransitionFailure` result.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return TransitionResult The result of the private mode transition, indicating success or failure.
     */
    context(StickfixBot)
    override fun onPrivateModeDisabled(): TransitionResult =
        handlePrivateModeTransition(
            mode = PrivateMode.DISABLED,
            successMessage = "User disabled private mode.",
            failureMessage = "Failed to disable private mode for user"
        )

    /**
     * Handles the transition for enabling or disabling private mode.
     *
     * @param mode The desired private mode state (enabled or disabled).
     * @param successMessage The log message to display on success.
     * @param failureMessage The log message to display on failure.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return TransitionResult The result of the private mode transition, indicating success or failure.
     */
    context(StickfixBot)
    private fun handlePrivateModeTransition(
        mode: PrivateMode,
        successMessage: String,
        failureMessage: String,
    ): TransitionResult {
        return databaseService.setPrivateMode(user, mode).fold(
            ifLeft = {
                logError(logger) { "$failureMessage ${user.debugInfo}: $it" }
                TransitionFailure(this)
            },
            ifRight = {
                logInfo(logger) { "$successMessage ${user.username}" }
                TransitionSuccess(user.state)
            }
        )
    }
}
