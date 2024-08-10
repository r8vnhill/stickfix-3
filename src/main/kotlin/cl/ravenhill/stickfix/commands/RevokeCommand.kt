package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.slf4j.LoggerFactory

/**
 * Represents a command to revoke a user's registration in the Stickfix bot. This command handles the logic for
 * confirming the user's intention to revoke their registration and updating the user's state accordingly.
 */
data object RevokeCommand : Command {

    /**
     * The name of the command, used for identifying and registering the command in the bot.
     */
    const val NAME = "revoke"

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Executes the revoke command. This method retrieves the user's information from the database, sends a confirmation
     * message to the user, and updates the user's state based on their response.
     *
     * @param user The `StickfixUser` instance representing the user who invoked the command.
     * @return `CommandResult` indicating the result of the command execution, which can be a success or failure.
     */
    context(StickfixBot)
    override fun invoke(user: StickfixUser): CommandResult = databaseService.getUser(user).fold(
        ifLeft = { handleUserNotRegistered(user) },
        ifRight = { handleUserRevocation(user) }
    )

    /**
     * Handles the scenario where a user attempts to revoke their registration but is not found in the database.
     * This function logs an error, attempts to send a message to the user informing them that they are not registered,
     * and returns a `CommandResult` indicating the outcome of the operation.
     *
     * @param user The `StickfixUser` instance representing the user attempting to revoke their registration.
     * @return `CommandResult` indicating the result of the operation. If the message is successfully sent to the user,
     *   the function returns a `CommandFailure` with details about the failure to revoke the user. If the message
     *   sending fails, the function logs the error and returns a `CommandFailure` with the appropriate error message.
     */
    context(StickfixBot)
    private fun handleUserNotRegistered(user: StickfixUser): CommandResult {
        logError(logger) { "User ${user.debugInfo} does not exist in the database, cannot revoke" }
        return sendMessage(user, "You are not registered in the database, cannot revoke").fold(
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
     * Handles the process of revoking a user's registration in the Stickfix bot. This function logs the revocation
     * action, sends a confirmation prompt to the user asking if they want to revoke their registration, and returns a
     * `CommandResult` indicating the outcome of this process.
     *
     * @param user The `StickfixUser` instance representing the user attempting to revoke their registration.
     * @return `CommandResult` indicating the result of the operation. If the message to confirm revocation is
     *   successfully sent, the function returns a `CommandSuccess` and transitions the user to the appropriate state.
     *   If the message sending fails, the function logs the error and returns a `CommandFailure` with the appropriate
     *   error message.
     */
    context(StickfixBot)
    private fun handleUserRevocation(user: StickfixUser): CommandResult {
        logInfo(logger) { "User ${user.debugInfo} revoked the bot" }
        return sendMessage(
            user = user,
            message = "Are you sure you want to revoke your registration?",
            replyMarkup = inlineKeyboardMarkup()
        ).fold(
            ifLeft = { failure ->
                logError(logger) { "Failed to send revoke prompt to user ${user.debugInfo}: $failure" }
                CommandFailure(user, "Failed to send message to user")
            },
            ifRight = {
                logInfo(logger) { "Sent revoke prompt to user ${user.debugInfo}" }
                user.onRevoke()
                CommandSuccess(user, "Revoke command sent successfully")
            }
        )
    }

    /**
     * Creates an inline keyboard markup with options for the user to confirm or cancel the revocation of their
     * registration.
     *
     * @return `InlineKeyboardMarkup` containing the "Yes" and "No" buttons for user input.
     */
    private fun inlineKeyboardMarkup() = InlineKeyboardMarkup.create(
        listOf(
            listOf(
                InlineKeyboardButton.CallbackData("Yes", RevokeConfirmationYes.name),
                InlineKeyboardButton.CallbackData("No", RevokeConfirmationNo.name)
            )
        )
    )
}
