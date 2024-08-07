package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.exceptions.DatabaseOperationException

/**
 * Represents the result of a database operation, encapsulating both a message and associated data. This sealed
 * interface allows for differentiation between successful and failed operations by providing specific implementations
 * for each case. The generic type `T` allows the result to carry any type of data, providing flexibility for various
 * use cases.
 *
 * ## Usage:
 * Use this interface and its implementations to standardize the results returned from database operations, ensuring
 * that each result includes a message and relevant data. This approach facilitates consistent error handling and
 * success reporting across the application.
 *
 * ### Example 1: Handling Database Operation Results
 * ```kotlin
 * fun handleDatabaseResult(result: DatabaseOperationResult<Any>) {
 *     when (result) {
 *         is DatabaseOperationSuccess -> println("Success: ${result.message} with data: ${result.data}")
 *         is DatabaseOperationFailure -> println("Failure: ${result.message} with error: ${result.data.message}")
 *     }
 * }
 * ```
 *
 * @param T The type of data associated with the database operation result.
 * @property message A string message describing the outcome of the operation.
 * @property data The data associated with the operation's result, providing additional context or information relevant
 *   to the outcome.
 */
sealed interface DatabaseOperationResult<T> {
    val message: String
    val data: T
}

/**
 * Represents a successful result from a database operation. This class provides a message describing the success and
 * any associated data.
 *
 * ## Usage:
 * Use this class to represent successful outcomes from database operations, ensuring that both a descriptive message
 * and relevant data are returned.
 *
 * ### Example 1: Creating a DatabaseOperationSuccess Result
 * ```kotlin
 * val successResult = DatabaseOperationSuccess("Operation completed successfully", data)
 * println(successResult.message)  // Outputs: "Operation completed successfully"
 * println(successResult.data)     // Outputs: data
 * ```
 *
 * @param T The type of data associated with the database operation result.
 * @property message A string message describing the successful outcome.
 * @property data The data associated with the successful operation's result.
 */
data class DatabaseOperationSuccess<T>(
    override val message: String,
    override val data: T
) : DatabaseOperationResult<T>

/**
 * Represents a failed result from a database operation. This class provides a message describing the failure and any
 * associated exception details.
 *
 * ## Usage:
 * Use this class to represent failed outcomes from database operations, ensuring that both a descriptive message and
 * relevant exception details are returned.
 *
 * ### Example 1: Creating a DatabaseOperationFailure Result
 * ```kotlin
 * val failureResult = DatabaseOperationFailure(
 *     "Operation failed due to a database error",
 *     DatabaseOperationException("Database connection timeout")
 * )
 * println(failureResult.message)  // Outputs: "Operation failed due to a database error"
 * println(failureResult.data.message)  // Outputs: "Database connection timeout"
 * ```
 *
 * @property message A string message describing the failure.
 * @property data The exception details associated with the failed operation's result.
 */
data class DatabaseOperationFailure(
    override val message: String,
    override val data: DatabaseOperationException
) : DatabaseOperationResult<DatabaseOperationException>
