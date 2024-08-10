/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser

/**
 * Represents a command in a Telegram bot. This sealed interface defines the essential structure for commands, ensuring
 * that each command has access to the user initiating the command and the bot processing it. Implementations of this
 * interface must define the logic for executing the command.
 */
sealed interface Command {

    /**
     * Executes the command. This operator function must be implemented by all subclasses to define the logic for
     * executing the command, interacting with necessary services and components.
     *
     * @param user The `StickfixUser` instance representing the user who initiated the command.
     * @return `CommandResult` indicating the result of the command execution, which can be a success or failure.
     */
    context(StickfixBot)
    operator fun invoke(user: StickfixUser): CommandResult
}
