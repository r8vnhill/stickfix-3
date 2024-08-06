/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import cl.ravenhill.stickfix.bot.TelegramBot.Companion.failedToSendMessage
import cl.ravenhill.stickfix.bot.TelegramBot.Companion.messageSentTo
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.StartCommand
import cl.ravenhill.stickfix.db.DatabaseService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StickfixBot")

/**
 * Implements the `TelegramBot` interface to provide specific functionalities for the Stickfix bot.
 * This class manages the bot's operational state, message sending, and initializes communication
 * protocols with Telegram's servers using the provided token.
 *
 * ## Usage:
 * This class should be instantiated with a valid Telegram bot token. The `start` method should be
 * called to begin listening for incoming messages and commands. Messages are sent using the
 * `sendMessage` method, which supports markdown formatting and optional interactive components.
 *
 * ### Example 1: Instantiating and Starting StickfixBot
 * ```kotlin
 * val bot = StickfixBot("your_bot_token_here")
 * println(bot.start())  // Outputs: "Bot started" or "Bot already started"
 * ```
 *
 * @property token The authentication token used for the Telegram API.
 */
class StickfixBot(override val databaseService: DatabaseService) : TelegramBot {
    /**
     * Tracks whether the bot has been started.
     */
    private var started: Boolean = false

    /**
     * A local instance of the bot initialized with the authentication token. This instance is used
     * to interface directly with the Telegram API.
     */
    private val _bot = bot {
        this@bot.token = databaseService.apiToken
        registerCommands(databaseService, this@StickfixBot)
    }

    /**
     * Starts the bot's operations if it hasn't been started already. This involves starting to poll
     * messages from Telegram's servers to listen for incoming commands and messages.
     *
     * @return
     *  A string message indicating whether the bot was successfully started or if it was already
     *  running.
     */
    override fun start(): String = if (started) {
        "Bot already started"
    } else {
        started = true
        _bot.startPolling()
        "Bot started"
    }

    /**
     * Sends a message to a specific user via the Telegram bot. The message can include markdown
     * formatting and optional interactive components such as keyboards.
     *
     * @param user The `ReadUser` instance representing the recipient of the message.
     * @param message The text of the message to send, which may include markdown formatting.
     * @param replyMarkup Optional parameter that allows adding interactive components to the message.
     * @return BotResult A result object indicating success or failure of the message sending operation.
     */
    override fun sendMessage(
        user: ReadUser,
        message: String,
        replyMarkup: ReplyMarkup?
    ) = _bot.sendMessage(
        ChatId.fromId(user.userId),
        message,
        parseMode = ParseMode.MARKDOWN_V2,
        replyMarkup = replyMarkup
    ).fold(
        ifError = { failedToSendMessage(user, message) },
        ifSuccess = { messageSentTo(user, message) }
    )
}

context(Bot.Builder)
private fun registerCommands(databaseService: DatabaseService, bot: TelegramBot) {
    dispatch {
        command(StartCommand.NAME) {
            when (val result =
                StartCommand(StickfixUser.from(message.from!!), bot, databaseService).execute()
            ) {
                is CommandSuccess -> logger.info("Start command executed successfully: $result")
                is CommandFailure -> logger.error("Start command failed: $result")
            }
        }
    }
}

