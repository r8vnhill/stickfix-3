/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import cl.ravenhill.stickfix.bot.dispatch.registerPrivateModeDisabledCallback
import cl.ravenhill.stickfix.bot.dispatch.registerPrivateModeEnabledCallback
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeCommand
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeConfirmationNo
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeConfirmationYes
import cl.ravenhill.stickfix.bot.dispatch.registerStartCommand
import cl.ravenhill.stickfix.bot.dispatch.registerStartConfirmationNo
import cl.ravenhill.stickfix.bot.dispatch.registerStartConfirmationYes
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.exceptions.MessageSendingException
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup

/**
 * Represents the Stickfix bot, integrating with a `StickfixDatabase` to manage user data and bot operations. This class
 * provides functionalities to start the bot, send messages to users, and register commands.
 *
 * @property databaseService The service used to manage user data and interactions. This service provides the bot's API
 *   token and handles command registration.
 */
class StickfixBot(val databaseService: StickfixDatabase) {
    /**
     * Tracks whether the bot has been started.
     */
    private var started: Boolean = false

    /**
     * A local instance of the bot initialized with the API token from the `DatabaseService`. Registers commands using
     * the provided database service and bot instance.
     */
    private val _bot = bot {
        this@bot.token = databaseService.queryApiKey().fold(
            ifLeft = { throw it.data },
            ifRight = { it.data }
        )
        registerCommands(this@StickfixBot)
    }

    /**
     * Starts the bot's operations if it hasn't been started already. This involves starting to poll messages from
     * Telegram's servers to listen for incoming commands and messages.
     *
     * @return A string message indicating whether the bot was successfully started or if it was already running.
     */
    fun start(): String = if (started) {
        "Bot already started"
    } else {
        started = true
        _bot.startPolling()
        "Bot started"
    }

    /**
     * Sends a message to a specific user via the Telegram bot. The message can include Markdown formatting
     * and optional interactive components such as keyboards.
     *
     * @param user The `ReadUser` instance representing the recipient of the message.
     * @param message The text of the message to send, which may include Markdown formatting.
     * @param replyMarkup Optional parameter that allows adding interactive components to the message.
     * @return Either a `BotSuccess` or `BotFailure` result indicating the success or failure of the message sending
     *   operation.
     */
    fun sendMessage(
        user: ReadUser,
        message: String,
        replyMarkup: ReplyMarkup? = null,
    ): Either<BotFailure<MessageSendingException>, BotSuccess<String>> =
        _bot.sendMessage(ChatId.fromId(user.userId), message, ParseMode.MARKDOWN, replyMarkup = replyMarkup).fold(
            ifSuccess = {
                BotSuccess(
                    "Message sent to user ${user.username.ifBlank { user.userId.toString() }}",
                    message
                ).right()
            },
            ifError = {
                BotFailure(
                    "Failed to send message to user ${user.username.ifBlank { user.userId.toString() }}",
                    MessageSendingException.from(it)
                ).left()
            }
        )
}

context(Bot.Builder)
private fun registerCommands(bot: StickfixBot) {
    dispatch {
        // region : Command registration
        registerStartCommand(bot)
        registerRevokeCommand(bot)
        // endregion
        // region : Callback query registration
        registerStartConfirmationYes(bot)
        registerStartConfirmationNo(bot)
        registerRevokeConfirmationYes(bot)
        registerRevokeConfirmationNo(bot)
        registerPrivateModeEnabledCallback(bot)
        registerPrivateModeDisabledCallback(bot)
        // endregion
    }
}
