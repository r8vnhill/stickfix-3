/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logDebug
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.logWarn
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
 * @param user The `StickfixUser` instance for whom the start command is being executed.
 * @return
 *  A string formatted to indicate that a start command is being executed for the given user,
 *  utilizing their debug information for identification.
 */
private fun initMessage(user: StickfixUser) = "Executing start command for user ${user.debugInfo}"

/**
 * Represents a command to start or register a user in the Stickfix bot. This command handles the logic for welcoming
 * back returning users or prompting new users to register.
 */
data object StartCommand : Command {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * The name of the command, used for identifying and registering the command in the bot.
     */
    const val NAME = "start"

    /**
     * Executes the start command. This method checks if the user is already registered and sends an appropriate
     * message. For new users, it sends a registration prompt. For returning users, it sends a welcome back message.
     *
     * @return `CommandResult` indicating the result of the command execution, which can be a success or failure.
     */
    context(StickfixBot)
    override fun invoke(user: StickfixUser): CommandResult {
        logInfo(logger) { initMessage(user) }
        return databaseService.getUser(user).fold(
            ifLeft = {
                logWarn(logger) { "User not found, sending registration prompt." }
                tempDatabase.addUser(user).fold(
                    ifLeft = { CommandFailure(user, "Failed to add user to temporary database.") },
                    ifRight = {
                        logDebug(logger) { "User added to temporary database: $it" }
                        sendRegistrationPrompt(user)
                    }
                )
            },
            ifRight = { sendWelcomeBackMessage(user) }
        )
    }

    /**
     * Sends a welcome back message to a returning user.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @return `CommandResult` indicating the success or failure of sending the welcome back message.
     */
    context(StickfixBot)
    private fun sendWelcomeBackMessage(user: StickfixUser): CommandResult {
        logInfo(logger) { "User is already registered, sending welcome back message." }
        return sendMessage(user, "Welcome back!").fold(
            ifLeft = { CommandFailure(user, "Failed to send welcome back message.") },
            ifRight = { CommandSuccess(user, "Welcome back message sent successfully.") }
        )
    }

    /**
     * Sends a registration prompt to a new user.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @return `CommandResult` indicating the success or failure of sending the registration prompt.
     */
    context(StickfixBot)
    private fun sendRegistrationPrompt(user: StickfixUser): CommandResult {
        val inlineKeyboardMarkup = inlineKeyboardMarkup()
        return sendMessage(user, welcomeMessage, inlineKeyboardMarkup).fold(
            ifLeft = {
                user.onIdle()
                CommandFailure(user, "Failed to send registration prompt.")
            },
            ifRight = {
                user.onStart()
                CommandSuccess(user, "Registration prompt sent.")
            }
        )
    }

    /**
     * Creates an inline keyboard markup with options for the user to confirm or cancel the registration process.
     *
     * @return `ReplyMarkup` containing the "Yes" and "No" buttons for user input.
     */
    private fun inlineKeyboardMarkup(): ReplyMarkup {
        val yesButton = InlineKeyboardButton.CallbackData("Yes", StartConfirmationYes.name)
        val noButton = InlineKeyboardButton.CallbackData("No", StartConfirmationNo.name)
        val row = listOf(yesButton, noButton)
        return InlineKeyboardMarkup.createSingleRowKeyboard(row)
    }
}
