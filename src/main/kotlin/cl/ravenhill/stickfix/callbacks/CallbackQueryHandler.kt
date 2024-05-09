/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Serves as a base class for handling callback queries in an application, particularly within the
 * context of interactions with a Telegram bot. This sealed class provides a structured way to
 * define distinct callback handlers, each responsible for a specific type of query. It enforces a
 * consistent interface for all handlers, which includes handling the callback query and returning a
 * result encapsulating the outcome of the operation.
 *
 * ## Usage:
 * Extend this class to create specific handlers for different types of callback queries. Each
 * handler must implement the abstract properties and methods defined herein, ensuring that they
 * provide the name of the handler and a method to process the callback query.
 *
 * ### Example 1: Implementing a CallbackQueryHandler
 * ```kotlin
 * class StartCommandHandler : CallbackQueryHandler() {
 *     override val name: String = "startCommand"
 *
 *     override fun invoke(
 *         user: ReadUser,
 *         bot: TelegramBot,
 *         dbService: DatabaseService
 *     ): CallbackResult {
 *         // Handle the start command
 *         return CallbackSuccess("Command processed successfully.")
 *     }
 * }
 * ```
 *
 * @property name
 *  A string that uniquely identifies the callback query handler. This is used to associate the
 *  handler with specific callback query types or commands.
 * @property logger
 *  A logger instance used for logging messages and debugging information related to the callback
 *  query handling.
 */
sealed class CallbackQueryHandler {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    abstract val name: String

    /**
     * Handles a callback query. This operator function must be implemented by all subclasses to process
     * the query specific to the handler's purpose, interacting with necessary services and components.
     *
     * @param user A `ReadUser` instance representing the user who initiated the callback query.
     * @param bot A `TelegramBot` instance involved in handling the callback query. This allows the handler
     * to interact with the Telegram API, such as sending messages or handling user responses.
     * @param dbService A `DatabaseService` instance used to perform any necessary database operations during
     * the callback query handling.
     * @return CallbackResult The result of processing the callback query, which can be a success or failure
     * depending on the operation performed.
     */
    abstract operator fun invoke(
        user: ReadUser,
        bot: TelegramBot,
        dbService: DatabaseService
    ): CallbackResult
}
