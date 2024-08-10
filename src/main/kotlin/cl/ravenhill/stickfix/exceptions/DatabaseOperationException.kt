package cl.ravenhill.stickfix.exceptions

/**
 * Represents an exception that occurs during database operations. This class extends the base `Exception` class,
 * providing additional context for errors specific to database interactions.
 *
 * ## Usage:
 * This class can be used to throw detailed exceptions when a database operation fails, providing a clear error message
 * that describes the nature of the failure.
 *
 * ### Example 1: Throwing a DatabaseOperationException
 * ```kotlin
 * try {
 *     // Code that might fail during a database operation
 *     throw DatabaseOperationException("Failed to update user data due to a database error")
 * } catch (e: DatabaseOperationException) {
 *     println(e.message)  // Outputs: "Failed to update user data due to a database error"
 * }
 * ```
 *
 * @param message A string describing the error message. This provides context for the error,
 *                making it easier to understand the reason for the failure.
 */
data class DatabaseOperationException(override val message: String) : Exception(message)
