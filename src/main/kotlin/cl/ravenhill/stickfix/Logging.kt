package cl.ravenhill.stickfix

/**
 * Logs an informational message using the provided SLF4J logger. The message is generated lazily using the provided
 * lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
inline fun logInfo(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.info(lazyMessage())

/**
 * Logs a debug message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
inline fun logDebug(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.debug(lazyMessage())

/**
 * Logs an error message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
inline fun logError(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.error(lazyMessage())

/**
 * Logs a warning message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
inline fun logWarn(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.warn(lazyMessage())

/**
 * Logs a trace message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
inline fun logTrace(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.trace(lazyMessage())
