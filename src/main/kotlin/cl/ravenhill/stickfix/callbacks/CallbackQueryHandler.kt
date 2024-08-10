/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
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
     * Processes a callback query for a specific user in the context of the Stickfix bot. This function retrieves the
     * user's data from the main database and determines the appropriate action based on whether the user is found or
     * not.
     *
     * @param user The `StickfixUser` instance representing the user who initiated the callback query.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the database operations.
     * @return `CallbackResult` The result of processing the callback query, which can either be a success or a failure
     *   depending on the user's registration status and the actions taken.
     */
    context(StickfixBot)
    operator fun invoke(user: StickfixUser): CallbackResult = databaseService.getUser(user.id).fold(
        ifLeft = { handleUserNotRegistered(user) },
        ifRight = { handleUserRegistered(it.data) }
    )

    /**
     * Handles the scenario where a user is successfully retrieved from the main database. This function must be
     * implemented by all subclasses to define the specific actions to take when the user is already registered.
     *
     * @param user The `StickfixUser` instance representing the user who is already registered in the main database.
     * @return A `CallbackResult` that indicates the outcome of the process, which can either be a success or a failure
     *   depending on the specific implementation.
     */
    context(StickfixBot)
    protected abstract fun handleUserRegistered(user: StickfixUser): CallbackResult

    /**
     * Handles the scenario where a user is not registered in the main database. This function must be implemented by
     * all subclasses to define the specific actions to take when the user is not registered.
     *
     * @param user The `StickfixUser` instance representing the user who is not registered in the main database.
     * @return A `CallbackResult` that indicates the outcome of the process, which can either be a success or a failure
     *   depending on the specific implementation.
     */
    context(StickfixBot)
    protected abstract fun handleUserNotRegistered(user: StickfixUser): CallbackResult
}
