package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.modes.ShuffleMode
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionSuccess

/**
 * Represents the base class for handling shuffle-related callback queries in the Stickfix bot application. This sealed
 * class defines the structure for handling specific shuffle-related actions, such as enabling or disabling shuffle mode.
 */
sealed class ShuffleCallback : CallbackQueryHandler()

/**
 * Handles the callback query for enabling shuffle mode in the Stickfix bot application. This object extends
 * `ShuffleCallback`, applying specific logic for users who enable shuffle mode. It updates the user's shuffle mode
 * setting in the database and sends a confirmation message.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object ShuffleEnabledCallback : ShuffleCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Handles the scenario where the user is registered in the system and successfully enables shuffle mode. This
     * function updates the user's shuffle mode setting in the database to `ENABLED`, logs the success, and sends a
     * confirmation message to the user.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @return CallbackResult The result of processing the callback query, indicating success or failure.
     */
    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CallbackResult {
        return databaseService.setShuffle(user, ShuffleMode.ENABLED).fold(
            ifLeft = {
                logError(logger) { "Failed to enable shuffle mode for user ${user.debugInfo}: $it" }
                CallbackFailure("Failed to enable shuffle mode for user ${user.debugInfo}.")
            },
            ifRight = {
                logInfo(logger) { "User ${user.username} enabled shuffle mode." }
                sendShuffleEnabledMessage(user)
            }
        )
    }

    /**
     * Sends a message to the user confirming that shuffle mode has been enabled.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API.
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @return CallbackResult The result of sending the message, indicating success or failure.
     */
    context(StickfixBot)
    private fun sendShuffleEnabledMessage(user: StickfixUser): CallbackResult {
        when (val result = user.onShuffleEnabled()) {
            is TransitionSuccess -> {
                val message = "Shuffle mode enabled. Your stickers will now be shuffled with each request."
                return sendMessage(user, message).fold(
                    ifLeft = { CallbackFailure("Failed to send shuffle enabled message to user ${user.debugInfo}.") },
                    ifRight = { CallbackSuccess("Shuffle mode enabled for user ${user.debugInfo}.") }
                )
            }
            is TransitionFailure -> {
                logError(logger) { "Failed to enable shuffle mode for user ${user.debugInfo}: $result" }
                return CallbackFailure("Failed to enable shuffle mode for user ${user.debugInfo}.")
            }
        }
    }

    /**
     * Handles the scenario where the user is not registered in the system. This function logs an error and returns a
     * failure result, indicating that shuffle mode cannot be enabled because the user is not registered.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @return CallbackResult The result of processing the callback query, indicating failure due to the user not being
     * registered.
     */
    context(StickfixBot) override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logError(logger) { "User ${user.username} is not registered. Cannot enable shuffle mode." }
        return CallbackFailure("User is not registered.")
    }
}

/**
 * Handles the callback query for disabling shuffle mode in the Stickfix bot application. This object extends
 * `ShuffleCallback`, applying specific logic for users who disable shuffle mode. It updates the user's shuffle mode
 * setting in the database and sends a confirmation message.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object ShuffleDisabledCallback : ShuffleCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Handles the scenario where the user is registered in the system and successfully disables shuffle mode. This
     * function updates the user's shuffle mode setting in the database to `DISABLED`, logs the success, and sends a
     * confirmation message to the user.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @return CallbackResult The result of processing the callback query, indicating success or failure.
     */
    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CallbackResult {
        return when (val result = user.onShuffleDisabled()) {
            is TransitionSuccess -> {
                logInfo(logger) { "User ${user.username} disabled shuffle mode." }
                sendShuffleDisabledMessage(user)
            }
            is TransitionFailure -> {
                logError(logger) { "Failed to disable shuffle mode for user ${user.debugInfo}: $result" }
                CallbackFailure("Failed to disable shuffle mode for user ${user.debugInfo}.")
            }
        }
    }

    /**
     * Sends a message to the user confirming that shuffle mode has been disabled.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API.
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @return CallbackResult The result of sending the message, indicating success or failure.
     */
    context(StickfixBot)
    private fun sendShuffleDisabledMessage(user: StickfixUser): CallbackResult {
        val message = "Shuffle mode disabled. Your stickers will no longer be shuffled with each request."
        return sendMessage(user, message).fold(
            ifLeft = { CallbackFailure("Failed to send shuffle disabled message to user ${user.debugInfo}.") },
            ifRight = { CallbackSuccess("Shuffle mode disabled for user ${user.debugInfo}.") }
        )
    }

    /**
     * Handles the scenario where the user is not registered in the system. This function logs an error and returns a
     * failure result, indicating that shuffle mode cannot be disabled because the user is not registered.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @return CallbackResult The result of processing the callback query, indicating failure due to the user not being
     *   registered.
     */
    context(StickfixBot) override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logError(logger) { "User ${user.username} is not registered. Cannot disable shuffle mode." }
        return CallbackFailure("User is not registered.")
    }
}
