/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import arrow.core.Either
import cl.ravenhill.jakt.constrainedTo
import cl.ravenhill.jakt.constraints.longs.BeEqualTo
import cl.ravenhill.jakt.exceptions.CompositeException
import cl.ravenhill.stickfix.MessageSendingException
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.RevokeCommand
import cl.ravenhill.stickfix.commands.StartCommand
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.db.schema.Users
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
 * Implements the `TelegramBot` interface, providing functionalities specific to the Stickfix bot.
 * This class integrates with a `DatabaseService` to manage user data and bot operations,
 * including starting the bot and sending messages to users.
 *
 * ## Usage:
 * Instantiate this class with a `DatabaseService` implementation to manage bot interactions and user data.
 * Use the `start` method to begin polling for messages and commands, and `sendMessage` to communicate
 * with users.
 *
 * ### Example 1: Instantiating and Starting StickfixBot
 * ```kotlin
 * val databaseService = MyDatabaseService()  // Assume MyDatabaseService is an implementation of DatabaseService
 * val bot = StickfixBot(databaseService)
 * println(bot.start())  // Outputs: "Bot started" or "Bot already started"
 * ```
 *
 * @property databaseService The service used to manage user data and interactions. This service provides the bot's API
 *   token and handles command registration.
 */
class StickfixBot(override val databaseService: DatabaseService) : TelegramBot {
    /**
     * Tracks whether the bot has been started.
     */
    private var started: Boolean = false

    /**
     * A local instance of the bot initialized with the API token from the `DatabaseService`.
     * Registers commands using the provided database service and bot instance.
     */
    private val _bot = bot {
        this@bot.token = queryApiKey(databaseService)
        registerCommands(databaseService, this@StickfixBot)
    }

    /**
     * Starts the bot's operations if it hasn't been started already. This involves starting to poll
     * messages from Telegram's servers to listen for incoming commands and messages.
     *
     * @return A string message indicating whether the bot was successfully started or if it was already running.
     */
    override fun start(): String = if (started) {
        "Bot already started"
    } else {
        started = true
        _bot.startPolling()
        "Bot started"
    }

    /**
     * Sends a message to a specific user via the Telegram bot. The message can include Markdown formatting and optional
     * interactive components such as keyboards.
     *
     * @param user The `ReadUser` instance representing the recipient of the message.
     * @param message The text of the message to send, which may include Markdown formatting.
     * @param replyMarkup Optional parameter that allows adding interactive components to the message.
     * @return BotResult A result object indicating success or failure of the message sending operation.
     */
    override fun sendMessage(user: ReadUser, message: String, replyMarkup: ReplyMarkup?) =
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
private fun registerCommands(databaseService: DatabaseService, bot: TelegramBot) {
    dispatch {
        registerStartCommand(databaseService, bot)
        registerRevokeCommand(databaseService, bot)
        registerStartConfirmationYes(databaseService, bot)
        registerStartConfirmationNo(databaseService, bot)
        registerRevokeConfirmationYes(databaseService, bot)
        registerRevokeConfirmationNo(databaseService, bot)
    }
}

context(Dispatcher)
private fun registerStartConfirmationYes(databaseService: DatabaseService, bot: TelegramBot) {
    callbackQuery(StartConfirmationYes.name) {
        val user = StickfixUser.from(callbackQuery.from)
        StartConfirmationYes(user, bot, databaseService)
    }
}

context(Dispatcher)
private fun registerStartConfirmationNo(databaseService: DatabaseService, bot: TelegramBot) {
    callbackQuery(StartConfirmationNo.name) {
        val user = StickfixUser.from(callbackQuery.from)
        StartConfirmationNo(user, bot, databaseService)
    }
}

context(Dispatcher)
private fun registerRevokeConfirmationYes(databaseService: DatabaseService, bot: TelegramBot) {
    callbackQuery(RevokeConfirmationYes.name) {
        val user = transaction {
            StickfixUser.from(Users.selectAll().where { Users.id eq callbackQuery.from.id }.single())
        }
        RevokeConfirmationYes.invoke(user, StickfixBot(databaseService), databaseService)
    }
}

context(Dispatcher)
private fun registerRevokeConfirmationNo(databaseService: DatabaseService, bot: TelegramBot) {
    callbackQuery(RevokeConfirmationNo.name) {
        val user = transaction {
            StickfixUser.from(Users.selectAll().where { Users.id eq callbackQuery.from.id }.single())
        }
        RevokeConfirmationNo.invoke(user, StickfixBot(databaseService), databaseService)
    }
}

context(Dispatcher)
private fun registerStartCommand(databaseService: DatabaseService, bot: TelegramBot) {
    command(StartCommand.NAME) {
        logger.info("Received start command from ${message.from}")
        when (val result = StartCommand(StickfixUser.from(message.from!!), bot, databaseService).execute()) {
            is CommandSuccess -> logger.info("Start command executed successfully: $result")
            is CommandFailure -> logger.error("Start command failed: $result")
        }
    }
}

context(Dispatcher)
private fun registerRevokeCommand(databaseService: DatabaseService, bot: TelegramBot) {
    command(RevokeCommand.NAME) {
        logger.info("Received revoke command from ${message.from}")
        when (val result = RevokeCommand(StickfixUser.from(message.from!!), bot, databaseService).execute()) {
            is CommandSuccess -> logger.info("Revoke command executed successfully: $result")
            is CommandFailure -> logger.error("Revoke command failed: $result")
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
private fun queryApiKey(databaseService: DatabaseService): String = transaction(databaseService.database) {
    val result = Meta.selectAll().where { Meta.key eq "API_KEY" }.constrainedTo {
        "API_KEY must be present in meta table" { it.count() must BeEqualTo(1L) }
    }
    result.single()[Meta.value]
}
