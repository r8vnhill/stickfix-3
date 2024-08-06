package cl.ravenhill.stickfix

import com.github.kotlintelegrambot.types.TelegramBotResult

/**
 * Represents an error that occurs when a message fails to send through the Telegram bot. This exception class extends
 * the base `Exception` class, providing additional context for message sending failures specific to Telegram bot
 * interactions.
 *
 * ## Usage:
 * This class can be used to throw detailed exceptions when a message sending operation fails. The companion object
 * provides a convenient method to create an instance of `MessageSendingException` from a `TelegramBotResult.Error`
 * object, ensuring consistency in error reporting.
 *
 * ### Example 1: Throwing a MessageSendingException
 * ```kotlin
 * try {
 *     // Code that might fail to send a message
 *     throw MessageSendingException("Failed to send message due to network error")
 * } catch (e: MessageSendingException) {
 *     println(e.message)  // Outputs: "Failed to send message due to network error"
 * }
 * ```
 *
 * ### Example 2: Creating a MessageSendingException from TelegramBotResult.Error
 * ```kotlin
 * val telegramError: TelegramBotResult.Error<*> = // Assume this is obtained from a failed operation
 * val error = MessageSendingException.from(telegramError)
 * println(error.message)  // Outputs: "Error sending message: $telegramError"
 * ```
 *
 * @param message A string describing the error message. This provides context for the error, making it easier to
 *   understand the reason for the failure.
 */
class MessageSendingException(message: String) : Exception(message) {
    /**
     * Companion object to provide additional factory methods for creating instances of `MessageSendingException`.
     */
    companion object {
        /**
         * Creates an instance of `MessageSendingException` from a `TelegramBotResult.Error` object. This method
         * extracts relevant information from the `TelegramBotResult.Error` and formats it into a human-readable error
         * message.
         *
         * @param telegramError The `TelegramBotResult.Error` object representing the error encountered during a
         *   Telegram bot operation.
         * @return An instance of `MessageSendingException` with a detailed error message.
         */
        fun from(telegramError: TelegramBotResult.Error<*>) =
            MessageSendingException("Error sending message: $telegramError")
    }
}
