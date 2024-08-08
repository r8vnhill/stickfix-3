/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.StickfixDatabase
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Represents a handler for callback queries in a Telegram bot. This sealed class provides a structure for defining
 * specific callback query handlers, each responsible for processing a particular type of callback query. It enforces a
 * consistent interface for all handlers, including a method to invoke the handler and process the query.
 *
 * @property logger A logger instance for logging callback query handling actions. This logger is protected and
 *   available to subclasses for logging purposes.
 * @property name A string that uniquely identifies the callback query handler. This is used to associate the handler
 *   with specific callback query commands.
 */
sealed class CallbackQueryHandler {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    abstract val name: String

    /**
     * Processes a callback query for a specific user and bot instance. This method must be implemented by all
     * subclasses to handle the callback query according to the handler's purpose.
     *
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @param bot The `StickfixBot` instance used to process the callback query and interact with the Telegram API.
     * @return `CallbackResult` indicating the result of processing the callback query, which can be a success or
     *   failure.
     */
    abstract operator fun invoke(user: StickfixUser, bot: StickfixBot): CallbackResult
}
