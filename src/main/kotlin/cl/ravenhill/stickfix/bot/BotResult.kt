/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

/**
 * Represents the result of a bot operation, encapsulating both a message and associated data. This sealed interface
 * allows for the differentiation between successful and failed operations by providing specific implementations for
 * each case. The generic type `T` allows the result to carry any type of data, providing flexibility for various use
 * cases.
 *
 * ## Usage:
 * Use this interface and its implementations to standardize the results returned from bot operations, ensuring that
 * each result includes a message and relevant data. This approach facilitates consistent error handling and success
 * reporting across the application.
 *
 * ### Example 1: Handling Bot Results
 * ```kotlin
 * fun handleBotResult(result: BotResult<Any>) {
 *     when (result) {
 *         is BotSuccess -> println("Success: ${result.message} with data: ${result.data}")
 *         is BotFailure -> println("Failure: ${result.message} with data: ${result.data}")
 *     }
 * }
 * ```
 *
 * @param T The type of data associated with the bot result.
 * @property message A string message describing the outcome of the operation.
 * @property data The data associated with the operation's result, providing additional context or information relevant
 *   to the outcome.
 */
sealed interface BotResult<T> {
    val message: String
    val data: T
}

/**
 * Represents a successful result from a bot operation. This class provides a message describing the success and any
 * associated data.
 *
 * ## Usage:
 * Use this class to represent successful outcomes from bot operations, ensuring that both a descriptive message and
 * relevant data are returned.
 *
 * ### Example 1: Creating a BotSuccess Result
 * ```kotlin
 * val successResult = BotSuccess("Operation completed successfully", data)
 * println(successResult.message)  // Outputs: "Operation completed successfully"
 * println(successResult.data)     // Outputs: data
 * ```
 *
 * @param T The type of data associated with the bot result.
 * @property message A string message describing the successful outcome.
 * @property data The data associated with the successful operation's result.
 */
data class BotSuccess<T>(
    override val message: String,
    override val data: T
) : BotResult<T>

/**
 * Represents a failed result from a bot operation. This class provides a message describing the failure and any
 * associated data that might help in diagnosing the issue.
 *
 * ## Usage:
 * Use this class to represent failed outcomes from bot operations, ensuring that both a descriptive message and
 * relevant data are returned.
 *
 * ### Example 1: Creating a BotFailure Result
 * ```kotlin
 * val failureResult = BotFailure("Operation failed due to an error", errorData)
 * println(failureResult.message)  // Outputs: "Operation failed due to an error"
 * println(failureResult.data)     // Outputs: errorData
 * ```
 *
 * @param T The type of data associated with the bot result.
 * @property message A string message describing the failure.
 * @property data The data associated with the failed operation's result, which might include error details or
 *   additional context for the failure.
 */
data class BotFailure<T>(
    override val message: String,
    override val data: T
) : BotResult<T>
