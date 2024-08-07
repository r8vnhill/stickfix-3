/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import arrow.core.Either
import cl.ravenhill.jakt.constrainedTo
import cl.ravenhill.jakt.constraints.longs.BeEqualTo
import cl.ravenhill.jakt.exceptions.CompositeException
import cl.ravenhill.stickfix.bot.dispatch.registerPrivateModeDisabledCallback
import cl.ravenhill.stickfix.bot.dispatch.registerPrivateModeEnabledCallback
import cl.ravenhill.stickfix.exceptions.MessageSendingException
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeCommand
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeConfirmationNo
import cl.ravenhill.stickfix.bot.dispatch.registerRevokeConfirmationYes
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.StartCommand
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.db.schema.Meta
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StickfixBot")

/**
 * Represents the Stickfix bot, integrating with a `StickfixDatabase` to manage user data and bot operations. This class
 * provides functionalities to start the bot, send messages to users, and register commands.
 *
 * ## Usage:
 * Instantiate this class with a `StickfixDatabase` implementation to manage bot interactions and user data. Use the
 * `start` method to begin polling for messages and commands, and `sendMessage` to communicate with users.
 *
 * ### Example 1: Instantiating and Starting StickfixBot
 * ```kotlin
 * val databaseService = StickfixDatabase("jdbcUrl", "driverName")
 * val bot = StickfixBot(databaseService)
 * println(bot.start())
 * ```
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
        this@bot.token = queryApiKey(databaseService)
        registerCommands(databaseService, this@StickfixBot)
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
    fun sendMessage(user: ReadUser, message: String, replyMarkup: ReplyMarkup? = null) =
        _bot.sendMessage(ChatId.fromId(user.userId), message, ParseMode.MARKDOWN, replyMarkup = replyMarkup).fold(
            ifSuccess = {
                Either.Left(
                    BotSuccess(
                        "Message sent to user ${user.username.ifBlank { user.userId.toString() }}",
                        message
                    )
                )
            },
            ifError = {
                Either.Right(
                    BotFailure(
                        "Failed to send message to user ${user.username.ifBlank { user.userId.toString() }}",
                        MessageSendingException.from(it)
                    )
                )
            }
        )
}

context(Bot.Builder)
private fun registerCommands(databaseService: StickfixDatabase, bot: StickfixBot) {
    dispatch {
        // region : Command registration
        registerStartCommand(databaseService, bot)
        registerRevokeCommand(databaseService, bot)
        // endregion
        // region : Callback query registration
        registerStartConfirmationYes(databaseService, bot)
        registerStartConfirmationNo(databaseService, bot)
        registerRevokeConfirmationYes(databaseService)
        registerRevokeConfirmationNo(databaseService)
        registerPrivateModeEnabledCallback(bot)
        registerPrivateModeDisabledCallback(bot)
        // endregion
    }
}

context(Dispatcher)
private fun registerStartConfirmationYes(databaseService: StickfixDatabase, bot: StickfixBot) {
    callbackQuery(StartConfirmationYes.name) {
        val user = StickfixUser.from(callbackQuery.from)
        StartConfirmationYes(user, bot, databaseService)
    }
}

context(Dispatcher)
private fun registerStartConfirmationNo(databaseService: StickfixDatabase, bot: StickfixBot) {
    callbackQuery(StartConfirmationNo.name) {
        val user = StickfixUser.from(callbackQuery.from)
        StartConfirmationNo(user, bot, databaseService)
    }
}

context(Dispatcher)
private fun registerStartCommand(databaseService: StickfixDatabase, bot: StickfixBot) {
    command(StartCommand.NAME) {
        logger.info("Received start command from ${message.from}")
        when (val result = StartCommand(StickfixUser.from(message.from!!), bot, databaseService).execute()) {
            is CommandSuccess -> logger.info("Start command executed successfully: $result")
            is CommandFailure -> logger.error("Start command failed: $result")
        }
    }
}

/**
 * Retrieves the API key from the `Meta` table of the database specifically where the key column equals "API_KEY".
 * This function ensures that exactly one entry for "API_KEY" is present in the database and returns its associated
 * value.
 * If the constraint is not met (i.e., if "API_KEY" is not present exactly once), an exception will be thrown.
 *
 * @return Returns the string value of the API key if found and valid.
 * @throws CompositeException If the constraint for the presence of "API_KEY" is not met.
 */
private fun queryApiKey(databaseService: StickfixDatabase): String = transaction(databaseService.database) {
    val result = Meta.selectAll().where { Meta.key eq "API_KEY" }.constrainedTo {
        "API_KEY must be present in meta table" { it.count() must BeEqualTo(1L) }
    }
    result.single()[Meta.value]
}
