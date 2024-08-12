/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Represents a command in a Telegram bot. This sealed interface defines the essential structure for commands, ensuring
 * that each command has access to the user initiating the command and the bot processing it. Implementations of this
 * interface must define the logic for executing the command.
 *
 * @property name The name of the command, used to identify it within the bot.
 * @property description A brief description of the command's purpose and functionality.
 */
sealed class Command(val name: String, val description: String) {

    /**
     * A logger instance for logging command-related actions. This logger is protected to allow subclasses to access it
     * for logging purposes.
     */
    protected val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
}
