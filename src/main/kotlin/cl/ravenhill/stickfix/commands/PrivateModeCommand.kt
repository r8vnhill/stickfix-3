package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.PrivateModeDisabledCallback
import cl.ravenhill.stickfix.callbacks.PrivateModeEnabledCallback
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.PrivateModeCommand.description
import cl.ravenhill.stickfix.commands.PrivateModeCommand.name
import cl.ravenhill.stickfix.handleUserAction
import cl.ravenhill.stickfix.handleUserNotRegistered
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

/**
 * Handles the command for enabling or disabling private mode in the Stickfix bot application. This command allows users
 * to toggle private mode, where all stickers added by the user will be private and only visible to them. The command
 * checks if the user is registered in the database and performs the appropriate actions based on their registration
 * status.
 *
 * @property name The name of the command, used to invoke it within the bot. This is set to "private".
 * @property description A brief description of what the command does, informing users that private mode will make all
 *   added stickers private and only visible to them.
 */
data object PrivateModeCommand : UserChatCommand() {
    override val name = "private"

    override val description =
        "Enable or disable private mode for your account. In private mode, all stickers you add will be private and " +
                "only you will be able to see them."

    /**
     * Handles the scenario where the user is not registered in the database and cannot enable private mode. This method
     * uses a helper function to log the appropriate error message, send a failure message to the user, and return a
     * `CommandResult` indicating that the operation failed.
     *
     * @param user The `StickfixUser` instance representing the user attempting to enable private mode.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `CommandResult` indicating that the operation failed because the user is not registered in the database.
     */
    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CommandResult =
        handleUserNotRegistered(
            user,
            action = "enable private mode",
            failureMessage = "You are not registered in the database, cannot enable private mode",
            logger = logger
        )

    /**
     * Handles the scenario where the user is registered in the database and successfully enables private mode. This
     * method sends a confirmation message to the user, indicating that private mode has been enabled, and returns a
     * `CommandResult` indicating success.
     *
     * @param user The `StickfixUser` instance representing the user enabling private mode.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `CommandResult` indicating that private mode has been successfully enabled for the user.
     */
    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CommandResult {
        return handleUserAction(
            user = user,
            actionDescription = "is setting private mode",
            message = "Do you want to enable private mode?",
            replyMarkup = inlineKeyboardMarkup(),
            logger = logger
        ) {
            onPrivateMode()  // Additional action to take on success
        }
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
                InlineKeyboardButton.CallbackData("Enable", PrivateModeEnabledCallback.name),
                InlineKeyboardButton.CallbackData("Disable", PrivateModeDisabledCallback.name)
            )
        )
    )
}
