package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.ShuffleDisabledCallback
import cl.ravenhill.stickfix.callbacks.ShuffleEnabledCallback
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.ShuffleCommand.description
import cl.ravenhill.stickfix.commands.ShuffleCommand.name
import cl.ravenhill.stickfix.utils.handleUserAction
import cl.ravenhill.stickfix.utils.handleUserNotRegistered
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

/**
 * Represents the command to enable shuffle mode for stickers in the Stickfix bot application. This command allows users
 * to enable shuffling of stickers on each request. When the command is invoked, the bot will prompt the user to confirm
 * their action via an inline keyboard. Depending on the user's registration status, the command will either proceed
 * with enabling shuffle mode or notify the user that they cannot enable shuffle mode if they are not registered.
 *
 * @property name The name of the command, used to invoke the shuffle mode command in the chat.
 * @property description A brief description of the command, explaining its purpose to users.
 */
data object ShuffleCommand : UserChatCommand() {

    override val name = "shuffle"

    override val description = "Enables shuffling of stickers on each request."

    /**
     * Handles the scenario where the user is not registered in the database. This method logs the failure, sends a
     * message to the user indicating that they cannot enable shuffle mode because they are not registered, and returns
     * a `CommandFailure` result.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @param user The `StickfixUser` instance representing the user who invoked the command.
     * @return CommandResult The result of the command execution, indicating failure due to the user not being registered.
     */
    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CommandResult =
        handleUserNotRegistered(
            user,
            action = "enable shuffle mode",
            failureMessage = "You are not registered in the database, cannot enable shuffle mode",
            logger = logger
        )

    /**
     * Handles the scenario where the user is registered in the database. This method prompts the user to confirm
     * enabling shuffle mode using an inline keyboard. Upon confirmation, the shuffle mode will be enabled for the user.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @param user The `StickfixUser` instance representing the user who invoked the command.
     * @return CommandResult The result of the command execution, indicating success or failure of enabling shuffle mode.
     */
    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CommandResult = handleUserAction(
        user = user,
        actionDescription = "enable shuffle mode",
        message = "Do you want to enable shuffle mode?",
        replyMarkup = inlineKeyboardMarkup(),
        logger = logger
    ) {
        onShuffle()
    }

    /**
     * Creates an inline keyboard markup with options to enable or disable shuffle mode.
     *
     * @return InlineKeyboardMarkup The inline keyboard markup with enable and disable options.
     */
    private fun inlineKeyboardMarkup() = InlineKeyboardMarkup.create(
        listOf(
            listOf(
                InlineKeyboardButton.CallbackData("Enable", ShuffleEnabledCallback.name),
                InlineKeyboardButton.CallbackData("Disable", ShuffleDisabledCallback.name)
            )
        )
    )
}
