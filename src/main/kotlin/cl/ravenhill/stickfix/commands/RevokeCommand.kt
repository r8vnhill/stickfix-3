package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.handleUserAction
import cl.ravenhill.stickfix.handleUserNotRegistered
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

/**
 * Represents a command to revoke a user's registration in the Stickfix bot. This command handles the logic for
 * confirming the user's intention to revoke their registration and updating the user's state accordingly.
 */
data object RevokeCommand : UserChatCommand() {

    override val name = "revoke"

    override val description = "Revoke your registration in the Stickfix bot"

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
    override fun handleUserNotRegistered(user: StickfixUser): CommandResult =
        handleUserNotRegistered(
            user,
            action = "revoke registration",
            failureMessage = "You are not registered in the database, cannot revoke registration",
            logger = logger
        )

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
    override fun handleUserRegistered(user: StickfixUser): CommandResult {
        return handleUserAction(
            user = user,
            actionDescription = "is revoking registration",
            message = "Are you sure you want to revoke your registration?",
            replyMarkup = inlineKeyboardMarkup(),
            logger = logger
        ) {
            onRevoke()  // Additional action to take on success
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
                InlineKeyboardButton.CallbackData("Yes", RevokeConfirmationYes.name),
                InlineKeyboardButton.CallbackData("No", RevokeConfirmationNo.name)
            )
        )
    )
}
