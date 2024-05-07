/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package bot

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
        "Bot already started"
    } else {
        started = true
        "Bot started"
    }
}
