/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.info
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** A detailed welcome message providing instructions and options for new users. */
private val welcomeMessage = """
            Welcome to *Stickfix*! 🎉
            To manage your stickers, the bot needs to register your chat id and username.
            This is a one-time process, and you can revoke the access at any time by typing `/revoke`.
            
            Send `Yes` to proceed or `No` to cancel the registration process (you can always 
            register later by typing `/start`).
            
            You can also use Stickfix without registering using the inline mode, but you will only
            have access to public stickers.
        """.trimIndent()

/**
 * Generates an initialization message indicating that a start command is being executed for a
 * specific user. This message is primarily used for logging purposes to trace the actions being
 * taken on behalf of a user at the start of a command execution.
 *
 * @param user The `ReadUser` instance for whom the start command is being executed.
 * @return
 *  A string formatted to indicate that a start command is being executed for the given user,
 *  utilizing their debug information for identification.
 */
private fun initMessage(user: ReadUser) = "Executing start command for user ${user.debugInfo}"

/**
 * Represents the command to start the Stickfix bot for a user. This command handles checking if the user is already
 * registered and sends either a welcome back message or a registration prompt accordingly. It implements the `Command`
 * interface, utilizing the provided bot instance, user information, and database service.
 *
 * @property user The `ReadUser` instance representing the user issuing the command. This provides read-only access to
 *   basic user information like username and user ID.
 * @property bot The `StickfixBot` instance representing the bot that processes the command. This allows the command to
 *   interact with the bot's functionalities, such as sending messages or performing actions on behalf of the user.
 * @property databaseService The `StickfixDatabase` instance used to interact with the database. This allows the command
 *   to perform necessary database operations as part of its execution.
 */
data class StartCommand(
    override val user: ReadUser,
    override val bot: StickfixBot,
    override val databaseService: StickfixDatabase,
) : Command {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Executes the start command, checking if the user is already registered and sending the appropriate message.
     * Logs the result of the command execution.
     *
     * @return CommandResult The result of the command execution, indicating success or failure along with any relevant
     *   messages.
     */
    override fun execute(): CommandResult {
        info(logger) { initMessage(user) }
        val registeredUser = databaseService.getUser(user)
        val result = if (registeredUser.data != null) {
            sendWelcomeBackMessage(user)
        } else {
            sendRegistrationPrompt(user)
        }
        info(logger) { "Start command result: $result" }
        return result
    }

    /**
     * Sends a welcome back message to the user if they are already registered.
     *
     * @param user The `ReadUser` instance representing the user.
     * @return CommandResult The result of the message sending operation, indicating success or failure.
     */
    private fun sendWelcomeBackMessage(user: ReadUser) = bot.sendMessage(user, "Welcome back!").fold(
        ifLeft = { CommandFailure(user, "Failed to send welcome back message.") },
        ifRight = { CommandSuccess(user, "Welcome back message sent successfully.") }
    )

    /**
     * Sends a registration prompt to the user if they are not already registered.
     *
     * @param user The `ReadUser` instance representing the user.
     * @return CommandResult The result of the message sending operation, indicating success or failure.
     */
    private fun sendRegistrationPrompt(user: ReadUser): CommandResult {
        val inlineKeyboardMarkup = inlineKeyboardMarkup()
        return bot.sendMessage(user, welcomeMessage, inlineKeyboardMarkup).fold(
            ifLeft = { CommandFailure(user, "Failed to send registration prompt.") },
            ifRight = {
                user.onStart(bot)
                CommandSuccess(user, "Registration prompt sent.")
            }
        )
    }

    /**
     * Creates an inline keyboard markup with "Yes" and "No" buttons for the registration prompt.
     *
     * @return ReplyMarkup The inline keyboard markup.
     */
    private fun inlineKeyboardMarkup(): ReplyMarkup {
        val yesButton = InlineKeyboardButton.CallbackData("Yes", StartConfirmationYes.name)
        val noButton = InlineKeyboardButton.CallbackData("No", StartConfirmationNo.name)
        val row = listOf(yesButton, noButton)
        return InlineKeyboardMarkup.createSingleRowKeyboard(row)
    }

    companion object {
        /**
         * The name of the command, used to identify and register the command within the bot.
         */
        const val NAME = "start"
    }
}
