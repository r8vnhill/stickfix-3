package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.modes.ShuffleMode

/**
 * Represents the shuffle state for a user in the Stickfix bot application. This state is responsible for handling the
 * transition when a user enables or disables shuffle mode. The shuffle mode allows users to manage or display their
 * stickers in a shuffled order.
 *
 * @property user The `StickfixUser` instance representing the user associated with this state.
 */
class ShuffleState(override val user: StickfixUser) : State() {

    /**
     * Handles the enabling of shuffle mode for the user. This method updates the user's shuffle mode in the database
     * to `ENABLED` and logs the appropriate messages. If the operation is successful, it transitions the user to the
     * next state. Otherwise, it returns a failure result.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return TransitionResult The result of enabling shuffle mode, indicating success or failure.
     */
    context(StickfixBot)
    override fun onShuffleEnabled(): TransitionResult {
        return handleShuffleTransition(
            mode = ShuffleMode.ENABLED,
            successMessage = "User enabled shuffle mode."
        )
    }

    /**
     * Handles the disabling of shuffle mode for the user. This method updates the user's shuffle mode in the database
     * to `DISABLED` and logs the appropriate messages. If the operation is successful, it transitions the user to the
     * next state. Otherwise, it returns a failure result.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return TransitionResult The result of disabling shuffle mode, indicating success or failure.
     */
    context(StickfixBot)
    override fun onShuffleDisabled(): TransitionResult {
        return handleShuffleTransition(
            mode = ShuffleMode.DISABLED,
            successMessage = "User disabled shuffle mode."
        )
    }

    /**
     * A helper method to handle the transition of shuffle mode (enabled or disabled) for the user. It updates the
     * user's shuffle mode in the database and logs the appropriate messages. If the operation is successful, it
     * transitions the user to the next state. Otherwise, it returns a failure result.
     *
     * @param mode The `ShuffleMode` enum value indicating whether shuffle mode is enabled or disabled.
     * @param successMessage The message to log upon successful transition.
     * @return TransitionResult The result of the shuffle mode transition, indicating success or failure.
     */
    context(StickfixBot)
    private fun handleShuffleTransition(
        mode: ShuffleMode,
        successMessage: String
    ): TransitionResult {
        return databaseService.setShuffle(user, mode).fold(
            ifLeft = {
                logError(logger) { "Failed to set shuffle mode for user: ${it.message}" }
                TransitionFailure(this)
            },
            ifRight = {
                logInfo(logger) { successMessage }
                TransitionSuccess(user.state)
            }
        )
    }
}
