/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.StickfixDatabase

/**
 * Represents a command to be executed within the Stickfix bot application. This sealed interface defines the essential
 * properties and method that any command must implement, ensuring consistent interaction with the bot, the user, and
 * the database service.
 *
 * ## Usage:
 * Implement this interface in classes designed to handle specific commands from users. Implementing classes must define
 * how the command is executed, utilizing the provided bot instance, user information, and database service.
 *
 * ### Example 1: Implementing a Command
 * ```kotlin
 * data class StartCommand(
 *     override val user: ReadUser,
 *     override val bot: StickfixBot,
 *     override val databaseService: StickfixDatabase
 * ) : Command {
 *     override fun execute(): CommandResult {
 *         // Command execution logic here
 *         return CommandSuccess(user, "Start command executed successfully")
 *     }
 * }
 * ```
 *
 * @property user The `ReadUser` instance representing the user issuing the command. This provides read-only access to
 *   basic user information like username and user ID.
 * @property bot The `StickfixBot` instance representing the bot that processes the command. This allows  the command to
 *   interact with the bot's functionalities, such as sending messages or performing actions on behalf of the user.
 * @property databaseService The `StickfixDatabase` instance used to interact with the database. This allows the command
 *   to perform necessary database operations as part of its execution.
 */
sealed interface Command {
    val user: ReadUser
    val bot: StickfixBot
    val databaseService: StickfixDatabase

    /**
     * Executes the command, performing the necessary actions and returning the result of the operation. Implementing
     * classes must define the specific logic for command execution, utilizing the provided bot instance, user
     * information, and database service.
     *
     * @return CommandResult The result of the command execution, indicating success or failure along with any relevant
     *   messages or data.
     */
    fun execute(): CommandResult
}
