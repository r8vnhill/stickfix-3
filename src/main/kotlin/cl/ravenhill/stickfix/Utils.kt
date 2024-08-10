package cl.ravenhill.stickfix

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandResult
import cl.ravenhill.stickfix.commands.CommandSuccess
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ReplyMarkup
import org.slf4j.Logger

/**
 * Handles the scenario where a user is not registered in the database and an action cannot be performed. This function
 * logs an error message indicating that the user is not registered, attempts to send a failure message to the user,
 * and returns a `CommandResult` indicating the outcome.
 *
 * @param user The `StickfixUser` instance representing the user that is not registered in the main database.
 * @param action A string describing the action that could not be performed because the user is not registered.
 * @param failureMessage The message to be sent to the user, explaining why the action could not be performed.
 * @param logger The logger instance used for logging errors and information related to this operation.
 * @receiver The `StickfixBot` instance, providing context for bot operations and access to the database service.
 * @return `CommandResult` indicating the outcome of the operation. It will be a `CommandFailure` indicating that the
 *   user is not registered and the failure message was sent, or a `CommandFailure` indicating that the message could
 *   not be sent.
 */
context(StickfixBot)
internal fun handleUserNotRegistered(
    user: StickfixUser,
    action: String,
    failureMessage: String,
    logger: Logger,
): CommandResult {
    logError(logger) { "User ${user.debugInfo} does not exist in the database, cannot $action" }
    return sendMessage(user, failureMessage).fold(
        ifLeft = { failure ->
            logError(logger) { "Failed to send message to user ${user.debugInfo}: $failure" }
            CommandFailure(user, "Failed to send message to user")
        },
        ifRight = { success ->
            logInfo(logger) { "Sent message to user ${user.debugInfo}" }
            CommandFailure(user, "User not registered in the database, message sent: $success")
        }
    )
}

/**
 * Registers a command in the Stickfix bot with a specified command name and execution logic. This function
 * encapsulates the common logic for handling commands, including user extraction, logging, and command execution.
 *
 * @param commandName The name of the command to register. This is the command that the user will trigger in the bot.
 * @param logger The logger instance used for logging the command handling actions. It logs the process of receiving
 *   the command, creating the user, and the result of the command execution.
 * @param commandExecution A lambda function that defines the execution logic for the command. It takes a `StickfixUser`
 *   as a parameter and returns a `CommandResult`, indicating the success or failure of the command execution.
 *
 * @receiver StickfixBot The bot instance that provides access to the bot's functionalities and database service.
 * @receiver Dispatcher The dispatcher context that allows the registration of commands and callback queries in the
 *   bot's command handling system.
 */
context(StickfixBot, Dispatcher)
internal inline fun registerCommand(
    commandName: String,
    logger: Logger,
    crossinline commandExecution: (StickfixUser) -> CommandResult
) {
    command(commandName) {
        // Extract the user from the message
        val user = message.from?.let {
            StickfixUser.from(it)
        }
        if (user == null) {
            logError(logger) { "Failed to create StickfixUser from message: $message" }
            return@command
        }
        // Log the received command and execute it
        logInfo(logger) { "Received $commandName command from ${user.debugInfo}" }
        when (val result = commandExecution(user)) {
            is CommandSuccess -> logInfo(logger) { "$commandName command executed successfully: $result" }
            is CommandFailure -> logError(logger) { "$commandName command failed: $result" }
        }
    }
}

/**
 * Handles a user action within the Stickfix bot by sending a prompt to the user and executing a specific success
 * operation if the message is sent successfully. This function provides a flexible mechanism to handle different
 * user actions that require sending a message with optional interactive components and performing additional
 * operations on success.
 *
 * @receiver StickfixBot The bot instance used to send the message and manage user actions.
 * @param user The `StickfixUser` instance representing the user performing the action.
 * @param actionDescription A string description of the action being performed, used for logging purposes.
 * @param message The text of the message to be sent to the user.
 * @param logger The logger instance used to log the action and any related messages.
 * @param replyMarkup The reply markup (e.g., inline keyboard) to be included with the message, providing interactive
 *   options for the user.
 * @param onSuccess A lambda function that is executed on the user object if the message is sent successfully. This
 *   function allows for additional operations to be performed on the user after the message is sent.
 * @return CommandResult The result of handling the user action, indicating success or failure. If the message is sent
 *   successfully, a `CommandSuccess` is returned. Otherwise, a `CommandFailure` is returned with an appropriate error
 *   message.
 */
context(StickfixBot)
internal fun handleUserAction(
    user: StickfixUser,
    actionDescription: String,
    message: String,
    logger: Logger,
    replyMarkup: ReplyMarkup,
    onSuccess: StickfixUser.() -> Unit
): CommandResult {
    logInfo(logger) { "User ${user.debugInfo} $actionDescription" }
    return sendMessage(chat = user, message = message, replyMarkup = replyMarkup).fold(
        ifLeft = { failure ->
            logError(logger) { "Failed to send prompt to user ${user.debugInfo}: $failure" }
            CommandFailure(user, "Failed to send message to user")
        },
        ifRight = {
            logInfo(logger) { "Sent prompt to user ${user.debugInfo}" }
            user.onSuccess()
            CommandSuccess(user, "$actionDescription command sent successfully")
        }
    )
}
