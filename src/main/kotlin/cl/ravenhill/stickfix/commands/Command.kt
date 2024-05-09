/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService

/**
 * Defines a common structure for commands within an application that interacts with a Telegram bot.
 * This sealed interface ensures that all implementing command types include essential information
 * about the user issuing the command and the bot that processes it. Being a sealed interface, it
 * enables exhaustive type checking of its subtypes. This is particularly useful for handling
 * commands polymorphically and ensuring type safety across different command implementations.
 *
 * ## Usage:
 * Implement this interface in any class that represents a specific type of command issued by a user
 * to a Telegram bot. Each command will have access to the user who initiated it and the bot that
 * will process it, ensuring that all necessary context for command execution is available.
 * Implementing classes must define the `execute` method, which should carry out the command's
 * specific logic and return a `CommandResult` indicating the outcome of the command.
 *
 * ### Example 1: Implementing the Command Interface
 * ```kotlin
 * class StartCommand(override val user: ReadUser, override val bot: TelegramBot) : Command {
 *     override fun execute(): CommandResult {
 *         // Logic to start a session or interaction with the bot
 *         return CommandSuccess(user, "Session started successfully.")
 *     }
 * }
 * ```
 *
 * @property user
 *  A `ReadUser` instance representing the user who issues the command. This provides read-only
 *  access to basic user information like username and user ID, ensuring that the command can be
 *  associated with the correct user.
 * @property bot
 *  A `TelegramBot` instance representing the bot that processes the command. This enables the
 *  command to utilize the bot's functionalities, such as sending messages or performing actions on
 *  behalf of the user.
 * @property databaseService
 *  A `DatabaseService` instance representing the service used to interact with the database. This
 *  allows the command to perform database operations, such as storing or retrieving user data.
 *  Implementing classes should provide the necessary database service instance when creating a
 *  command object.
 */
sealed interface Command {
    val user: ReadUser
    val bot: TelegramBot
    val databaseService: DatabaseService

    /**
     * Executes the specific logic associated with the command and returns a `CommandResult`
     * indicating the outcome. This method is crucial for implementing the operational logic
     * specific to each command type.
     */
    fun execute(): CommandResult
}
