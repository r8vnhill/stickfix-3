/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

/**
 * Represents the outcome of a bot operation, encapsulated within a sealed interface to ensure
 * all potential results are comprehensively covered. This design allows for exhaustive when
 * expression checks without needing an else clause, facilitating safer and more predictable error
 * handling and success messaging.
 *
 * ## Usage:
 * Use this sealed interface in functions or processes where bot operations return varied results
 * that need clear categorization as success or failure. Each implementation carries a message that
 * describes the result, providing direct feedback or action details.
 *
 * ### Example 1: Handling Bot Results
 * ```kotlin
 * fun handleBotOperation(result: BotResult) {
 *     when (result) {
 *         is BotSuccess -> println("Success: ${result.message}")
 *         is BotFailure -> println("Error: ${result.message}")
 *     }
 * }
 * ```
 *
 * @property message
 *  A string that contains a detailed message describing the outcome of the bot operation. This can
 *  include success confirmations, error messages, or other operational feedback.
 */
sealed interface BotResult {
    val message: String
}

/**
 * Represents a successful outcome of a bot operation, containing a message that likely describes
 * the successful execution or results.
 *
 * @param message The message describing the specifics of the success. This could detail the actions
 * taken or the data processed by the bot.
 */
data class BotSuccess(override val message: String) : BotResult

/**
 * Represents a failure in a bot operation, containing a message that explains the nature of the
 * failure. This can assist in debugging or informing the user of what went wrong.
 *
 * @param message The message detailing the failure, which could include error details or why the
 * operation could not be completed successfully.
 */
data class BotFailure(override val message: String) : BotResult
