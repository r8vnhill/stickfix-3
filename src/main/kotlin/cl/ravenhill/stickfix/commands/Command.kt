/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.StickfixDatabase

/**
 * Represents a command to be executed within the Stickfix bot application. This sealed interface defines the essential
 * properties and method that any command must implement, ensuring consistent interaction with the bot, the user, and
 * the database service.
 *
 * @property user The `ReadUser` instance representing the user issuing the command. This provides read-only access to
 *   basic user information like username and user ID.
 * @property bot The `StickfixBot` instance representing the bot that processes the command. This allows  the command to
 *   interact with the bot's functionalities, such as sending messages or performing actions on behalf of the user.
 */
sealed interface Command {
    val user: ReadUser
    val bot: StickfixBot

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
