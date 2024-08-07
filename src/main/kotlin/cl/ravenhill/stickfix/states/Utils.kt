package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotFailure
import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("states.Utils")

/**
 * Handles common confirmation actions within the Stickfix bot application, performing additional operations,
 * sending confirmation messages, and setting the user's state to idle.
 *
 * ## Usage:
 * Call this function to handle confirmation actions that require additional operations, logging, and sending
 * confirmation messages to the user.
 *
 * ### Example 1: Using handleCommonConfirmation
 * ```kotlin
 * val result = handleCommonConfirmation(bot, "Action confirmed.", user) {
 *     // Additional database operations
 *     Users.update {
 *         it[someColumn] = someValue
 *     }
 * }
 * println(result.message)  // Outputs the result of the confirmation handling
 * ```
 *
 * @param bot The `StickfixBot` instance used to send messages to the user.
 * @param message The confirmation message to be sent to the user.
 * @param context The `ReadWriteUser` instance representing the user who confirmed the action.
 * @param additionalOperations The additional operations to be performed within a database transaction.
 * @return BotResult<*> The result of the confirmation handling, indicating success or failure.
 */
fun handleCommonConfirmation(
    bot: StickfixBot,
    message: String,
    context: ReadWriteUser,
    additionalOperations: Transaction.() -> Unit,
): BotResult<*> = transaction {
    // Perform additional operations specified by the caller
    additionalOperations()

    // Log the confirmation action
    logger.info("User ${context.username.ifBlank { context.userId.toString() }} confirmed action")

    // Send a confirmation message to the user and handle the result
    bot.sendMessage(context, message).also {
        // Set the user's state to idle
        context.onIdle(bot)

        // Verify the user's state and handle any errors
        verifyUserState(
            it.fold(
                { result -> result },
                { error -> throw error.data }
            ), IdleState::class.simpleName!!, context
        )
    }.fold(
        { it },      // Return the result if successful
        {
            BotFailure(
                "Failed to send confirmation message to user",
                it.data
            )
        }  // Return a failure if there was an error
    )
}

/**
 * Handles common rejection actions within the Stickfix bot application, performing additional operations,
 * sending rejection messages, and setting the user's state to idle.
 *
 * ## Usage:
 * Call this function to handle rejection actions that require additional operations, logging, and sending
 * rejection messages to the user.
 *
 * ### Example 1: Using handleCommonRejection
 * ```kotlin
 * val result = handleCommonRejection(bot, "Action denied.", user) {
 *     // Additional database operations
 *     Users.update {
 *         it[someColumn] = someValue
 *     }
 * }
 * println(result.message)  // Outputs the result of the rejection handling
 * ```
 *
 * @param bot The `StickfixBot` instance used to send messages to the user.
 * @param message The rejection message to be sent to the user.
 * @param context The `ReadWriteUser` instance representing the user who denied the action.
 * @param additionalOperations The additional operations to be performed within a database transaction.
 * @return BotResult<*> The result of the rejection handling, indicating success or failure.
 */
fun handleCommonRejection(
    bot: StickfixBot,
    message: String,
    context: ReadWriteUser,
    additionalOperations: Transaction.() -> Unit,
): BotResult<*> = transaction {
    // Perform additional operations specified by the caller
    additionalOperations()

    // Log the rejection action
    logger.info("User ${context.username.ifBlank { context.userId.toString() }} denied action")

    // Send a rejection message to the user and handle the result
    bot.sendMessage(context, message).also {
        // Set the user's state to idle
        context.onIdle(bot)

        // Verify the user's state and handle any errors
        verifyUserDeletion(
            it.fold(
                { result -> result },
                { error -> throw error.data }
            ), context
        )
    }.fold(
        { it },      // Return the result if successful
        {
            BotFailure(
                "Failed to send rejection message to user",
                it.data
            )
        }  // Return a failure if there was an error
    )
}
