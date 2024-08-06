/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo.name
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes.name
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService

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
 * A base class for handling the start confirmation process in an application using a Telegram bot.
 * This sealed class defines common functionalities and structures that are shared across different
 * types of start confirmations, providing a uniform approach to initiating operations and handling
 * responses.
 *
 * `StartConfirmation` extends `CallbackQueryHandler`, leveraging its callback handling framework
 * to interact with users through the Telegram bot. It encapsulates the logic for sending messages
 * to users and processing the responses, effectively managing both successful and unsuccessful outcomes.
 *
 * ## Usage:
 * Subclasses of `StartConfirmation` are designed to implement specific behaviors for different confirmation
 * scenarios. They utilize the `sendMessage` method to effectively communicate with users and handle the logic
 * based on the user's response.
 *
 * ### Example 1: Extending StartConfirmation
 * Here's how you might define a subclass that handles a specific type of start confirmation:
 * ```kotlin
 * object ConfirmStartSession : StartConfirmation() {
 *     override val name: String = "ConfirmStartSession"
 *
 *     override fun invoke(
 *         user: ReadUser,
 *         bot: TelegramBot,
 *         dbService: DatabaseService
 *     ): CallbackResult {
 *         val message = "Would you like to start the session?"
 *         return sendMessage(bot, user, message)
 *     }
 * }
 * ```
 */
sealed class StartConfirmation : CallbackQueryHandler() {

    /**
     * Handles sending a message to a user via the Telegram bot and processes the response,
     * returning a `CallbackResult`. This method centralizes error handling and response logging,
     * ensuring that messages are sent correctly and that failures are thoroughly logged.
     *
     * @param bot An instance of `TelegramBot` used to send messages to the user.
     * @param user The `ReadUser` instance representing the user to whom the message is being sent.
     * @param message The message to be sent to the user.
     * @return CallbackResult Either `CallbackSuccess` if the message is sent successfully, or
     *  `CallbackFailure` if the message fails to send, encapsulating the error message.
     */
    protected fun sendMessage(bot: TelegramBot, user: ReadUser, message: String) =
        bot.sendMessage(user, message).fold(
            { CallbackSuccess(message) },
            { error ->
                logger.error(errorSendingMessageLog(user, error))
                CallbackFailure(error.message)
            }
        )
}


/**
 * Handles the affirmative response to a start confirmation query in an application using a
 * Telegram bot. This object extends `StartConfirmation`, applying specific logic for users who
 * confirm a start action. It checks if the user is already registered and either registers them or
 * notifies them of their current status.
 *
 * ## Usage:
 * This class is triggered when a user sends a confirmation to start or activate a service or
 * process. It interacts with the database to verify user registration and communicates with the
 * user through the Telegram bot based on the user's registration status.
 *
 * ### Example 1: Using StartConfirmationYes
 * ```kotlin
 * // This example assumes a scenario where StartConfirmationYes
 * // is invoked through a user interaction flow.
 * val result = StartConfirmationYes(user, bot, dbService)
 * println(result.message)  // Output depends on the user's registration status
 * ```
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object StartConfirmationYes : StartConfirmation() {
    override val name = this::class.simpleName!!

    /**
     * Handles the logic when a user confirms the intention to start or register. It checks the
     * user's registration status and responds appropriately.
     *
     * @param user
     *  A `ReadUser` instance representing the user interacting with the bot.
     * @param bot
     *  A `TelegramBot` instance used to send messages back to the user.
     * @param dbService
     *  A `DatabaseService` instance for accessing and updating user registration information.
     * @return CallbackResult
     *  The result of the operation, indicating whether the process was successful or if the user
     *  was already registered, with appropriate messages delivered via the bot.
     */
    override fun invoke(
        user: ReadUser,
        bot: TelegramBot,
        dbService: DatabaseService,
    ): CallbackResult {
        // Retrieve user from database or register new user if not found
        val registeredUser = dbService.getUser(user)
        val message = if (registeredUser == null) {
            logger.info(registeringUserLog(user))
            dbService.addUser(user)
            WELCOME_MESSAGE
        } else {
            ALREADY_REGISTERED_MESSAGE
        }

        // Send appropriate message to user via Telegram bot
        return sendMessage(bot, user, message)
    }
}

/**
 * Handles the negative response to a start confirmation query in an application using a Telegram
 * bot. This object extends `StartConfirmation`, applying specific logic for users who explicitly
 * decline to start or register for a service. It informs users that they can register at a later
 * time and logs their decision for future reference.
 *
 * ## Usage:
 * Triggered in user interaction flows where users are given the choice to confirm or decline starting
 * or registering for a service. This class is responsible for sending a message that acknowledges the
 * user's decision not to proceed and reminds them that the option to register remains available.
 *
 * ### Example 1: Using StartConfirmationNo
 * ```kotlin
 * val user = ReadUserImpl("username", 1L) // Example user implementation
 * val bot = TelegramBotImpl("bot_token") // Example bot implementation
 * val dbService = DatabaseServiceImpl() // Example database service implementation
 * val result = StartConfirmationNo(user, bot, dbService)
 * println(result.message)  // Outputs: "You have chosen not to register. Remember you can always register later!"
 * ```
 *
 * @property name The simple name of this class, used for logging and referencing within the system.
 */
data object StartConfirmationNo : StartConfirmation() {
    override val name = this::class.simpleName!!

    /**
     * Processes the user's negative confirmation to start or register. It logs the user's decision
     * and sends them a message confirming their choice, while reminding them of the possibility to
     * register later.
     *
     * @param user A `ReadUser` instance representing the user interacting with the bot.
     * @param bot A `TelegramBot` instance used to send messages back to the user.
     * @param dbService A `DatabaseService` instance, although not directly used, included for
     * consistency and potential future use.
     * @return CallbackResult The result of the operation, indicating the message was successfully
     * sent with a confirmation of the user's decision, or detailing any failure that occurred.
     */
    override fun invoke(
        user: ReadUser,
        bot: TelegramBot,
        dbService: DatabaseService,
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
