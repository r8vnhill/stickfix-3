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
 *
 * @property user The `StickfixUser` instance representing the user who initiated the revoke command.
 * @property bot The `StickfixBot` instance used to process the command and interact with the Telegram API.
 */
data class RevokeCommand(
    override val user: StickfixUser,
    override val bot: StickfixBot
) : Command {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Executes the revoke command. This method retrieves the user's information from the database, sends a confirmation
     * message to the user, and updates the user's state based on their response.
     *
     * @return `CommandResult` indicating the result of the command execution, which can be a success or failure.
     */
    override fun invoke(): CommandResult {
        logInfo(logger) { "User ${user.username.ifBlank { user.userId.toString() }} revoked the bot" }
        val result = bot.databaseService.getUser(user).fold(
            ifLeft = {
                logError(logger) { "Failed to retrieve user: ${it.message}" }
                CommandFailure(user, "User does not exist in the database, cannot revoke")
            },
            ifRight = {
                val message = "Are you sure you want to revoke your registration?"
                bot.sendMessage(user, message, replyMarkup = inlineKeyboardMarkup())
                user.onRevoke(bot)
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

    companion object {
        /**
         * The name of the command, used for identifying and registering the command in the bot.
         */
        const val NAME = "revoke"
    }
}
