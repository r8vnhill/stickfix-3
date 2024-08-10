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
    override fun invoke(user: StickfixUser): CommandResult {
        logInfo(logger) { "User ${user.username.ifBlank { user.id.toString() }} revoked the bot" }
        val result = databaseService.getUser(user).fold(
            ifLeft = {
                logError(logger) { "Failed to retrieve user: ${it.message}" }
                CommandFailure(user, "User does not exist in the database, cannot revoke")
            },
            ifRight = {
                val message = "Are you sure you want to revoke your registration?"
                sendMessage(user, message, replyMarkup = inlineKeyboardMarkup())
                user.onRevoke()
                CommandSuccess(user, "Revoke command sent successfully")
            }
        )
        logInfo(logger) { "Revoke command result: $result" }
        return result
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
