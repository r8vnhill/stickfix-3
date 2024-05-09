/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import cl.ravenhill.stickfix.chat.ReadUser
import com.github.kotlintelegrambot.entities.ReplyMarkup

/**
 * Defines the essential structure and functionalities for a Telegram bot. This interface
 * specifies the necessary properties and methods that any Telegram bot implementation must support,
 * ensuring consistent and secure interactions with the Telegram Bot API.
 *
 * ## Usage:
 * Implement this interface in classes that are designed to represent a Telegram bot. Implementing
 * classes must manage the bot's authentication token and handle basic bot operations such as
 * sending messages and starting the bot.
 *
 * ### Example 1: Implementing the TelegramBot Interface
 * ```kotlin
 * class MyBot(override val token: String) : TelegramBot {
 *     override fun start(): String {
 *         // Connection and setup logic here
 *         return "Bot started"
 *     }
 *
 *     override fun sendMessage(user: ReadUser, message: String, replyMarkup: ReplyMarkup?): BotResult {
 *         // Message sending logic here
 *         return TelegramBot.messageSentTo(user, message)
 *     }
 * }
 * ```
 *
 * @property token
 *  A string representing the authentication token for the Telegram bot. This token is crucial for
 *  making authenticated requests to the Telegram Bot API and must be securely managed by the
 *  implementing class.
 */
interface TelegramBot {
    val token: String

    /**
     * Initiates the bot's primary operations necessary for it to start functioning. This includes
     * connecting to the Telegram API and setting up listeners for incoming commands or messages.
     *
     * @return A string message indicating the status of the bot upon attempting to start, typically
     *         confirming that the bot has started or providing an error message.
     */
    fun start(): String

    /**
     * Sends a message to a specified user through the Telegram bot. Optionally includes reply markup
     * to offer interactive options to the user.
     *
     * @param user The `ReadUser` instance representing the recipient of the message.
     * @param message The text of the message to be sent.
     * @param replyMarkup Optional `ReplyMarkup` providing interactive components such as inline
     *                    keyboards. Default is null if no interactive components are needed.
     * @return BotResult A result object indicating the success or failure of the send operation,
     *                   typically containing a message detailing the operation's outcome.
     */
    fun sendMessage(user: ReadUser, message: String, replyMarkup: ReplyMarkup? = null): BotResult

    companion object {
        /**
         * Generates a success result indicating that a message has been sent to a user.
         *
         * @param user The user to whom the message was sent.
         * @param message The message that was sent.
         * @return BotSuccess A `BotSuccess` result containing a descriptive success message.
         */
        fun messageSentTo(user: ReadUser, message: String) =
            BotSuccess("Message sent to ${user.debugInfo}: $message")

        /**
         * Generates a failure result indicating that a message failed to be sent to a user.
         * This typically occurs when the bot encounters an error while attempting to send a
         * message.
         *
         * @param user The user to whom the message was intended to be sent.
         * @param message The message that failed to be sent.
         */
        fun failedToSendMessage(user: ReadUser, message: String) =
            BotFailure("Failed to send message to ${user.debugInfo}: $message")
    }
}

