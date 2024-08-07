/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo.name
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes.name
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.StickfixDatabase

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
private fun errorSendingMessageLog(user: ReadUser, result: BotResult<*>) =
    "Failed to send message to ${user.debugInfo}: ${result.message}"

/**
 * Generates a standardized success log message for successfully sent messages to users.
 * This utility is used to maintain consistent and clear logging for successful interactions
 * with users through the application's messaging system, aiding in monitoring and verifying
 * successful communications.
 *
 * @param user
 *  The user to whom the message was successfully sent. User details are utilized for logging
 *  purposes.
 * @return
 *  A formatted string suitable for logging, which includes the user's identifier, confirming the
 *  success of the message delivery.
 */
private fun successSendingMessageLog(user: ReadUser) =
    "Message sent successfully to ${user.debugInfo}"

/**
 * Generates a standardized log message for registering new users in the system.
 *
 * @param user
 *  The user to be registered in the system. User details are utilized for logging purposes.
 */
private fun registeringUserLog(user: ReadUser) = "Registering new user: ${user.debugInfo}"

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
     * @param user The `ReadUser` instance representing the recipient of the message.
     * @param message The text of the message to send to the user.
     * @return CallbackResult The result of the message sending operation, indicating success or failure along with any
     *   relevant messages.
     */
    protected fun sendMessage(bot: StickfixBot, user: ReadUser, message: String) =
        bot.sendMessage(user, message).fold(
            ifLeft = { CallbackSuccess(message) },
            ifRight = { error ->
                logger.error(errorSendingMessageLog(user, error))
                CallbackFailure(error.message)
            }
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
     * status and responds appropriately.
     *
     * @param user A `ReadUser` instance representing the user interacting with the bot.
     * @param bot A `StickfixBot` instance used to send messages back to the user.
     * @param databaseService A `StickfixDatabase` instance for accessing and updating user registration information.
     * @return CallbackResult The result of the operation, indicating whether the process was successful or if the user
     *   was already registered, with appropriate messages delivered via the bot.
     */
    override fun invoke(
        user: ReadUser,
        bot: StickfixBot,
        databaseService: StickfixDatabase,
    ): CallbackResult {
        // Retrieve user from database or register new user if not found
        val registeredUser = databaseService.getUser(user)
        val message = if (registeredUser.data == null) {
            logger.info(registeringUserLog(user))
            databaseService.addUser(user)
            WELCOME_MESSAGE
        } else {
            ALREADY_REGISTERED_MESSAGE
        }

        // Send appropriate message to user via Telegram bot
        return sendMessage(bot, user, message)
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
     * @param user A `ReadUser` instance representing the user interacting with the bot.
     * @param bot A `StickfixBot` instance used to send messages back to the user.
     * @param databaseService A `StickfixDatabase` instance for accessing and updating user registration information.
     * @return CallbackResult The result of the operation, indicating the user's choice not to register, with
     *   appropriate messages delivered via the bot.
     */
    override fun invoke(
        user: ReadUser,
        bot: StickfixBot,
        databaseService: StickfixDatabase,
    ): CallbackResult {
        val logMessage = "User ${user.debugInfo} chose not to register."
        logger.info(logMessage)
        val message = "You have chosen not to register. Remember you can always register later!"

        return sendMessage(bot, user, message).also {
            if (it is CallbackSuccess) {
                // Log successful confirmation of the action to provide clear audit trails
                logger.info(logMessage) // Repeat log only on success to confirm action
            }
        }
    }
}
