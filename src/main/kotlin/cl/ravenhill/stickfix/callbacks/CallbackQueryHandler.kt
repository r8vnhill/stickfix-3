/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.StickfixDatabase
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Represents a handler for processing callback queries in the Stickfix bot application. This sealed class provides the
 * necessary structure and logging capabilities for handling various types of callback queries. Subclasses must define
 * the specific logic for handling callback queries by implementing the abstract properties
 * and methods.
 *
 * @property logger A logger instance for logging actions related to callback query handling. This logger is used to
 *   record activities such as processing queries and handling errors.
 */
sealed class CallbackQueryHandler {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * The name of the callback query handler, used for identifying the specific type of callback query being handled.
     * Subclasses must provide a unique name for each handler.
     */
    abstract val name: String

    /**
     * Handles the callback query, performing the necessary actions and returning the result of the operation.
     * Subclasses must implement this operator function to define the specific logic for handling the callback query.
     *
     * @param user The `ReadUser` instance representing the user initiating the callback query. This provides read-only
     *   access to basic user information like username and user ID.
     * @param bot The `StickfixBot` instance representing the bot that processes the callback query. This allows the
     *   handler to interact with the bot's functionalities, such as sending messages or performing actions on behalf of
     *   the user.
     * @return CallbackResult The result of handling the callback query, indicating success or failure along with any
     *   relevant messages or data.
     */
    abstract operator fun invoke(user: ReadUser, bot: StickfixBot): CallbackResult
}
