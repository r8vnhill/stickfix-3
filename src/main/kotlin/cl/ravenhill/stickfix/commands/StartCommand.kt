/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.BotFailure
import cl.ravenhill.stickfix.bot.BotSuccess
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
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
 * Handles the command to start interactions with a Telegram bot. This class manages user
 * registration and welcome messages, distinguishing between new and returning users.
 *
 * @property user
 *  The user performing the command.
 * @property bot
 *  The Telegram bot instance used for sending messages.
 * @property databaseService
 *  The service for accessing user data in the database.
 * @constructor
 *  Creates a new instance of the StartCommand class, initializing it with the user, bot, and
 *  database service.
 */
data class StartCommand(
    override val user: ReadUser,
    override val bot: TelegramBot,
    override val databaseService: DatabaseService
) : Command {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Executes the start command. It checks if the user exists in the database to either send a
     * welcome back message or a new registration prompt. Logs actions and outcomes for operational
     * monitoring.
     *
     * @return [CommandResult]
     *  Encapsulates the outcome of executing the start command, including user information and a
     *  message. Returns [CommandSuccess] on successful message delivery, and [CommandFailure] if
     *  the message fails to send.
     */
    override fun execute(): CommandResult {
        logger.info(initMessage(user))
        val registeredUser = databaseService.getUser(user)
        val result = if (registeredUser != null) {
            sendWelcomeBackMessage(user)
        } else {
            sendRegistrationPrompt(user)
        }
        logger.info("Start command result: $result")
        return result
    }

    /**
     * Sends a welcome back message to the user.
     *
     * @param user The user to send the message to.
     * @return
     *  Returns a [CommandResult] object encapsulating the outcome of sending the message. Returns
     *  [CommandSuccess] on successful message delivery, and [CommandFailure] if the message fails
     *  to send.
     */
    private fun sendWelcomeBackMessage(user: ReadUser): CommandResult {
        return when (bot.sendMessage(user, "Welcome back!")) {
            is BotFailure -> CommandFailure(user, "Failed to send welcome back message.")
            is BotSuccess -> CommandSuccess(user, "Welcome back message sent successfully.")
        }
    }

    /**
     * Sends a registration prompt to a new user along with an inline keyboard for interaction.
     * This function is responsible for guiding unregistered users through the registration process,
     * enhancing user engagement and facilitating the onboarding process.
     *
     * @param user
     *  The `ReadUser` instance representing the user to whom the registration prompt is being sent.
     * @return
     *  Returns a [CommandSuccess] indicating the success of sending the registration prompt,
     *  encapsulating the user information and a message.
     */
    private fun sendRegistrationPrompt(user: ReadUser): CommandResult {
        val inlineKeyboardMarkup = inlineKeyboardMarkup()
        bot.sendMessage(user, welcomeMessage, inlineKeyboardMarkup)
        user.onStart(bot)
        return CommandSuccess(user, "Registration prompt sent.")
    }


    /**
     * Creates an inline keyboard markup for displaying a row of buttons, enhancing user interaction.
     *
     * @return ReplyMarkup The created inline keyboard markup with "Yes" and "No" options.
     */
    private fun inlineKeyboardMarkup(): ReplyMarkup {
        val yesButton = InlineKeyboardButton.CallbackData("Yes", StartConfirmationYes.name)
        val noButton = InlineKeyboardButton.CallbackData("No", StartConfirmationNo.name)
        val row = listOf(yesButton, noButton)
        return InlineKeyboardMarkup.createSingleRowKeyboard(row)
    }

    companion object {
        const val NAME = "/start"
    }
}

