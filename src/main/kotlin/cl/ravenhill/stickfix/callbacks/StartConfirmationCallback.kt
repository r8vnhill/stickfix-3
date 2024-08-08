/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo.name
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes.name
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo

/**
 * A constant representing the welcome message sent to users when they successfully register  with
 * Stickfix via the Telegram bot. This message is intended to greet users and confirm their
 * successful registration or initiation.
 */
private const val WELCOME_MESSAGE = "Welcome to Stickfix!"

/**
 * A constant string message representing the response when a user is already registered.
 */
private const val ALREADY_REGISTERED_MESSAGE = "You are already registered!"

/**
 * Generates a standardized error log message when there is a failure in sending messages to users.
 * This function formats the error message using user details and the error message from the bot result.
 *
 * @param user
 *  The user to whom the message sending failed. User details are utilized for logging purposes.
 * @param result
 *  The result of the bot operation, containing the error message.
 * @return
 *  A formatted string suitable for logging, which includes the user's identifier and the error
 *  message.
 */
private fun errorSendingMessageLog(user: StickfixUser, result: BotResult<*>) =
    "Failed to send message to ${user.debugInfo}: ${result.message}"

/**
 * Generates a standardized log message for registering new users in the system.
 *
 * @param user
 *  The user to be registered in the system. User details are utilized for logging purposes.
 */
private fun registeringUserLog(user: StickfixUser) = "Registering new user: ${user.debugInfo}"

/**
 * Represents a handler for processing start confirmation callback queries in the Stickfix bot application. This sealed
 * class extends the `CallbackQueryHandler` class, providing additional functionality for sending messages to users.
 * Subclasses must define the specific logic for handling start confirmation queries by implementing the abstract
 * properties and methods from `CallbackQueryHandler`.
 *
 * @property logger A logger instance for logging actions related to callback query handling. This logger is used to
 *   record activities such as processing queries and handling errors.
 */
sealed class StartConfirmationCallback : CallbackQueryHandler() {

    /**
     * Sends a message to the user via the bot and returns the appropriate `CallbackResult`. This method handles logging
     * and error management, ensuring that any issues encountered during message sending are properly logged and
     * reported.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @param user The `StickfixUser` instance representing the recipient of the message.
     * @param message The text of the message to send to the user.
     * @return CallbackResult The result of the message sending operation, indicating success or failure along with any
     *   relevant messages.
     */
    protected fun sendMessage(bot: StickfixBot, user: StickfixUser, message: String) =
        bot.sendMessage(user, message).fold(
            ifLeft = { error ->
                logger.error(errorSendingMessageLog(user, error))
                CallbackFailure(error.message)
            },
            ifRight = { CallbackSuccess(message) },
        )
}

/**
 * Handles the affirmative response to a start confirmation query in the Stickfix bot application. This object extends
 * `StartConfirmationCallback`, applying specific logic for users who confirm a start action. It checks if the user is
 * already registered and either registers them or notifies them of their current status.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object StartConfirmationYes : StartConfirmationCallback() {
    override val name = this::class.simpleName!!

    /**
     * Handles the logic when a user confirms the intention to start or register. It checks the user's registration
     * status and responds appropriately by either registering the user or notifying them that they are already
     * registered.
     *
     * @param user A `StickfixUser` instance representing the user interacting with the bot.
     * @param bot A `StickfixBot` instance used to send messages back to the user.
     * @return CallbackResult The result of the operation, indicating whether the process was successful or if the user
     *   was already registered, with appropriate messages delivered via the bot.
     */
    override fun invoke(
        user: StickfixUser,
        bot: StickfixBot,
    ): CallbackResult {
        val databaseService = bot.databaseService
        return databaseService.getUser(user).fold(
            ifLeft = { error ->
                logError(logger) { "Failed to retrieve user data for ${user.debugInfo}: ${error.message}" }
                logInfo(logger) { registeringUserLog(user) }
                databaseService.addUser(user)
                val message = WELCOME_MESSAGE
                sendMessage(bot, user, message)
            },
            ifRight = { userResult ->
                logInfo(logger) { "Retrieved user data for ${user.debugInfo}: ${userResult.data}" }
                sendMessage(bot, user, ALREADY_REGISTERED_MESSAGE)
            }
        )
    }
}

/**
 * Handles the negative response to a start confirmation query in the Stickfix bot application. This object extends
 * `StartConfirmationCallback`, applying specific logic for users who decline a start action. It sends a message to the
 * user confirming their choice and logs the action.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object StartConfirmationNo : StartConfirmationCallback() {
    override val name = this::class.simpleName!!

    /**
     * Handles the logic when a user declines the intention to start or register. It sends a message confirming the
     * user's choice and logs the action.
     *
     * @param user A `StickfixUser` instance representing the user interacting with the bot.
     * @param bot A `StickfixBot` instance used to send messages back to the user.
     * @return CallbackResult The result of the operation, indicating the user's choice not to register, with
     *   appropriate messages delivered via the bot.
     */
    override fun invoke(user: StickfixUser, bot: StickfixBot): CallbackResult {
        user.onRejection(bot)
        val logMessage = "User ${user.debugInfo} chose not to register."
        logInfo(logger) { logMessage }
        val message = "You have chosen not to register. Remember you can always register later!"

        return sendMessage(bot, user, message).also {
            if (it is CallbackSuccess) {
                user.onIdle(bot)
                logInfo(logger) { "User ${user.debugInfo} set to idle state" }
            }
        }
    }
}
