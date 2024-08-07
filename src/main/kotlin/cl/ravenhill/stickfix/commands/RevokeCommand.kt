package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.error
import cl.ravenhill.stickfix.info
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.slf4j.LoggerFactory

/**
 * Represents the command to revoke the Stickfix bot for a user. This command handles checking if the user exists in the
 * database and, if so, sends a confirmation prompt to revoke their registration. It implements the `Command` interface,
 * utilizing the provided bot instance, user information, and database service.
 *
 * @property user The `ReadUser` instance representing the user issuing the command. This provides read-only access to
 *   basic user information like username and user ID.
 * @property bot The `StickfixBot` instance representing the bot that processes the command. This allows the command to
 *   interact with the bot's functionalities, such as sending messages or performing actions on behalf of the user.
 */
data class RevokeCommand(override val user: ReadUser, override val bot: StickfixBot) : Command {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Executes the revoke command, checking if the user exists in the database and sending a confirmation prompt if
     * they do. Logs the result of the command execution.
     *
     * @return CommandResult The result of the command execution, indicating success or failure along with any relevant
     *   messages.
     */
    override fun execute(): CommandResult {
        info(logger) { "User ${user.username.ifBlank { user.userId.toString() }} revoked the bot" }
        val result = bot.databaseService.getUser(user).fold(
            ifLeft = {
                error(logger) { "Failed to retrieve user: ${it.message}" }
                CommandFailure(user, "User does not exist in the database, cannot revoke")
            },
            ifRight = {
                val message = "Are you sure you want to revoke your registration?"
                bot.sendMessage(user, message, replyMarkup = inlineKeyboardMarkup())
                user.onRevoke(bot)
                CommandSuccess(user, "Revoke command sent successfully")
            }
        )
        info(logger) { "Revoke command result: $result" }
        return result
    }

    /**
     * Creates an inline keyboard markup with "Yes" and "No" buttons for the revoke confirmation prompt.
     *
     * @return InlineKeyboardMarkup The inline keyboard markup.
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
         * The name of the command, used to identify and register the command within the bot.
         */
        const val NAME = "revoke"
    }
}
