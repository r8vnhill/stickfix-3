/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import cl.ravenhill.stickfix.bot.TelegramBot.Companion.messageSentTo
import cl.ravenhill.stickfix.chat.ReadUser
import com.github.kotlintelegrambot.entities.ReplyMarkup

/**
 * Represents a constant string value indicating that the bot has already been started.
 */
private const val ALREADY_STARTED = "Bot already started"

/**
 * Represents a constant string value indicating that the bot has been started successfully.
 */
private const val BOT_STARTED = "Bot started"

/**
 * A simple implementation of the `TelegramBot` interface that represents a local Telegram bot.
 * This class uses a basic mechanism to prevent multiple starts of the bot, ensuring that it only
 * starts once.
 *
 * @constructor
 *  Creates a new instance of `LocalBot` with the specified authentication token for the Telegram
 *  API.
 * @param token
 *  A string representing the authentication token for the Telegram bot. This token is used to
 *  authenticate requests made to the Telegram Bot API.
 * @property token
 *  Overrides the `token` property from the `TelegramBot` interface to provide the authentication
 *  token.
 * @property started A private boolean property indicating whether the bot has already been started
 *  to prevent multiple starts.
 */
class LocalBot(override val token: String) : TelegramBot {
    /**
     * Represents the status of whether the bot has been started.
     */
    private var started: Boolean = false

    /**
     * Starts the bot's operations, ensuring the bot is only started once. This method checks if the
     * bot has already been started and returns an appropriate message.
     *
     * @return
     *  A string message indicating the status of the bot - either "Bot already started" if the
     *  bot has already been started, or "Bot started" if it was not previously started.
     */
    override fun start(): String = if (started) {
        ALREADY_STARTED
    } else {
        started = true
        BOT_STARTED
    }

    /**
     * Sends a message to a specified user.
     *
     * This method is an implementation of the message sending capability, simulating the sending
     * process as it always returns a success result without actual communication to external
     * services.
     *
     * @param user
     *  The `ReadUser` instance representing the user to whom the message is being sent.
     * @param message
     *  The string containing the message to be sent to the user.
     * @param replyMarkup
     *  Optional parameter for reply markup (such as keyboard options), used in the Telegram API to
     *  provide interactive components with the message.
     * @return
     *  BotResult Returns a [BotSuccess] with a standard message indicating that the message was
     *  sent. This simplifies interaction handling, especially in test environments.
     */
    override fun sendMessage(
        user: ReadUser,
        message: String,
        replyMarkup: ReplyMarkup?
    ) = messageSentTo(user, message)
}
