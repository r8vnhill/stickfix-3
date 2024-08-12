/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import cl.ravenhill.stickfix.bot.dispatch.registerHelpCommand
import cl.ravenhill.stickfix.bot.dispatch.registerPrivateModeCommand
import cl.ravenhill.stickfix.bot.dispatch.registerPrivateModeDisabledCallback
import cl.ravenhill.stickfix.bot.dispatch.registerPrivateModeEnabledCallback
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeCommand
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeConfirmationNo
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeConfirmationYes
import cl.ravenhill.stickfix.bot.dispatch.registerShuffleCommand
import cl.ravenhill.stickfix.bot.dispatch.registerShuffleDisabledCallback
import cl.ravenhill.stickfix.bot.dispatch.registerShuffleEnabledCallback
import cl.ravenhill.stickfix.bot.dispatch.registerStartCommand
import cl.ravenhill.stickfix.bot.dispatch.registerStartConfirmationNo
import cl.ravenhill.stickfix.bot.dispatch.registerStartConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixChat
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.db.TempDatabase
import cl.ravenhill.stickfix.exceptions.MessageSendingException
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.Dispatcher
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
     * A `TempDatabase` instance used for managing temporary user data. This database is initialized immediately upon
     * bot creation using the `init()` method, making it ready for use when handling temporary or transient user data.
     */
    val tempDatabase = TempDatabase().apply {
        init()
    }

    /**
     * A local instance of the bot initialized with the API token from the `DatabaseService`. Registers commands using
     * the provided database service and bot instance.
     */
    private val _bot = bot {
        this@bot.token = databaseService.queryApiKey().fold(
            ifLeft = { throw it.data },
            ifRight = { it.data }
        )
        registerCommands()
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
     * @param chat The `StickfixUser` instance representing the recipient of the message.
     * @param message The text of the message to send, which may include Markdown formatting.
     * @param replyMarkup Optional parameter that allows adding interactive components to the message.
     * @return Either a `BotSuccess` or `BotFailure` result indicating the success or failure of the message sending
     *   operation.
     */
    fun sendMessage(
        chat: StickfixChat,
        message: String,
        replyMarkup: ReplyMarkup? = null,
    ): Either<BotFailure<MessageSendingException>, BotSuccess<String>> =
        _bot.sendMessage(ChatId.fromId(chat.id), message, ParseMode.MARKDOWN, replyMarkup = replyMarkup).fold(
            ifSuccess = {
                BotSuccess(message = "Message sent to user ${chat.debugInfo}", data = message).right()
            },
            ifError = {
                BotFailure(
                    message = "Failed to send message to user ${chat.debugInfo}",
                    data = MessageSendingException.from(it)
                ).left()
            }
        )
}

/**
 * Registers all commands and callback queries for the Stickfix bot. This function organizes the registration process by
 * dispatching command and callback registrations to separate functions, ensuring modular and maintainable code.
 *
 * @receiver StickfixBot The bot instance used to register commands and callbacks.
 * @receiver Bot.Builder The bot builder instance used for configuring the bot's behavior.
 */
context(StickfixBot, Bot.Builder)
private fun registerCommands() {
    dispatch {
        registerAllCommands()
        registerCallbacks()
    }
}

/**
 * Registers all bot commands for the Stickfix bot. This function organizes the individual command registrations,
 * enabling the bot to handle various commands such as start, revoke, private mode, help, and shuffle commands.
 *
 * @receiver StickfixBot The bot instance used to register commands.
 * @receiver Dispatcher The dispatcher used for routing commands.
 */
context(StickfixBot, Dispatcher)
private fun registerAllCommands() {
    registerStartCommand()
    registerRevokeCommand()
    registerPrivateModeCommand()
    registerHelpCommand()
    registerShuffleCommand()
    registerAddCommand()
}

/**
 * Registers all callback queries for the Stickfix bot. This function organizes the individual callback query
 * registrations, enabling the bot to handle various callback actions such as start confirmation, revocation, private
 * mode, and shuffle mode callbacks.
 *
 * @receiver StickfixBot The bot instance used to register callback queries.
 * @receiver Dispatcher The dispatcher used for routing callback queries.
 */
context(StickfixBot, Dispatcher)
private fun registerCallbacks() {
    registerStartConfirmationYes()
    registerStartConfirmationNo()
    registerRevokeConfirmationYes()
    registerRevokeConfirmationNo()
    registerPrivateModeEnabledCallback()
    registerPrivateModeDisabledCallback()
    registerShuffleEnabledCallback()
    registerShuffleDisabledCallback()
}
