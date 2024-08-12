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
 * Represents a command to start or register a user in the Stickfix bot. This command handles the logic for welcoming
 * back returning users or prompting new users to register.
 */
data object StartCommand : UserChatCommand(
    name = "start",
    description = "Starts the registration process for the user."
) {

    /**
     * Handles the scenario where a user is already registered in the Stickfix bot application. This function logs an
     * informational message indicating that the user is already registered and sends a welcome back message to the
     * user.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @param user The `StickfixUser` instance representing the registered user.
     * @return `CommandResult` indicating the outcome of sending the welcome back message to the user. The result can
     *   indicate success or failure, depending on whether the message was sent successfully.
     */
    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CommandResult {
        logInfo(logger) { "User is already registered, sending welcome back message." }
        return sendWelcomeBackMessage(user)
    }

    /**
     * Handles the scenario where a user is not registered in the Stickfix bot application. This function logs a warning
     * message indicating that the user is not found and attempts to add the user to the temporary database. If the user
     * is successfully added to the temporary database, it sends a registration prompt to the user.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @param user The `StickfixUser` instance representing the user that is not registered.
     * @return `CommandResult` indicating the outcome of the operation. If the user is successfully added to the
     *   temporary database, the function returns a `CommandSuccess` result indicating that the registration prompt was
     *   sent. If the operation fails, it returns a `CommandFailure` result.
     */
    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CommandResult {
        logWarn(logger) { "User not found, sending registration prompt." }
        return tempDatabase.addUser(user).fold(
            ifLeft = { CommandFailure(user, "Failed to add user to temporary database.") },
            ifRight = {
                logDebug(logger) { "User added to temporary database: $it" }
                sendRegistrationPrompt(user)
            }
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
