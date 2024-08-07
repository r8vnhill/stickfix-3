package cl.ravenhill.stickfix

/**
 * Logs an informational message using the provided SLF4J logger. The message is generated lazily using the provided
 * lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
fun info(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.info(lazyMessage())

/**
 * Logs a debug message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
fun debug(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.debug(lazyMessage())

/**
 * Logs an error message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
fun error(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.error(lazyMessage())

/**
 * Logs a warning message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
fun warn(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.warn(lazyMessage())

/**
 * Logs a trace message using the provided SLF4J logger. The message is generated lazily using the provided lambda.
 *
 * @param logger The SLF4J logger instance used to log the message.
 * @param lazyMessage A lambda function that generates the message to be logged.
 */
fun trace(logger: org.slf4j.Logger, lazyMessage: () -> String) = logger.trace(lazyMessage())
