/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.chat.StickfixUser

/**
 * Represents the result of a command executed within an application, encapsulating the outcome
 * along with associated user information and a message describing the result. As a sealed
 * interface, `CommandResult` ensures that all possible outcomes of a command are represented
 * within a closed set of types, facilitating type-safe handling of different command outcomes.
 *
 * ## Usage:
 * Use this interface to define a common structure for handling the outcomes of commands issued to a
 * system. Implementations of `CommandResult` can be used to communicate success or failure of
 * command executions, along with relevant messages. This is particularly useful in scenarios where
 * commands are processed and feedback is required.
 *
 * ### Example 1: Handling Command Results
 * ```kotlin
 * fun processCommandResult(result: CommandResult) {
 *     when (result) {
 *         is CommandSuccess -> println("Success: ${result.message}")
 *         is CommandFailure -> println("Failure: ${result.message}")
 *     }
 * }
 * ```
 *
 * @property user
 *  A `StickfixUser` instance representing the user associated with the command result. This provides
 *  read-only access to basic user information, ensuring that the result can be linked back to the
 *  appropriate user.
 * @property message
 *  A string containing a message that describes the outcome of the command, providing feedback
 *  or additional information about the result.
 */
sealed interface CommandResult {
    val user: StickfixUser
    val message: String
}

/**
 * Represents a successful outcome of a command with a user-associated message.
 *
 * @param user The user associated with the command success.
 * @param message A descriptive message about the success of the command.
 */
data class CommandSuccess(
    override val user: StickfixUser,
    override val message: String
) : CommandResult

/**
 * Represents a failure outcome of a command with a user-associated message.
 *
 * @param user The user associated with the command failure.
 * @param message A descriptive message about the failure of the command.
 */
data class CommandFailure(
    override val user: StickfixUser,
    override val message: String
) : CommandResult
