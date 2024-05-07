/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package bot

/**
 * Defines the structure for a Telegram bot by specifying the required properties that any
 * implementing class must provide. This interface ensures that all Telegram bot implementations
 * have an accessible token used for authenticating API requests.
 *
 * ## Usage:
 * Implement this interface in any class that represents a Telegram bot. The implementing class must
 * provide a way to retrieve the Telegram bot's token, which is essential for making authenticated
 * requests to the Telegram Bot API.
 *
 * @property token
 *  A string representing the authentication token for the Telegram bot. This token is used to
 *  authenticate requests made to the Telegram Bot API.
 */
interface TelegramBot {
    val token: String

    /**
     * Starts the bot's operations. This method is responsible for initiating the primary operations
     * required to get the bot running, such as connecting to the Telegram API and setting up
     * listeners for incoming commands or messages.
     */
    fun start(): String
}
