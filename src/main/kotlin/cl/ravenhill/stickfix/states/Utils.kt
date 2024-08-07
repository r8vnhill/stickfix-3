package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotFailure
import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("states.Utils")

/**
 * Handles common confirmation actions for different bot commands. This function performs additional operations, logs
 * the confirmation, sends a message to the user, and updates the user's state to idle. It uses a transaction to ensure
 * database consistency.
 *
 * ## Usage:
 * This function is used to handle common confirmation actions such as enabling private mode or registering a user. It
 * encapsulates the common logic required for these operations and allows for specific additional operations to be
 * performed.
 *
 * ### Example 1: Using handleCommonConfirmation for Private Mode
 * ```kotlin
 * private fun handlePrivateModeConfirmation(bot: TelegramBot, context: ReadWriteUser): BotResult<*> {
 *     return handleCommonConfirmation(bot, "Private mode has been enabled.", context) {
 *         Users.update {
 *             it[privateMode] = true
 *         }
 *     }
 * }
 * ```
 *
 * ### Example 2: Using handleCommonConfirmation for Registration
 * ```kotlin
 * private fun handleRegistrationConfirmation(bot: TelegramBot, context: ReadWriteUser): BotResult<*> {
 *     return handleCommonConfirmation(bot, "You were successfully registered!", context) {
 *         // Additional operations for registration confirmation can be added here
 *     }
 * }
 * ```
 *
 * @param bot The `TelegramBot` instance used to send messages to the user.
 * @param message The message to be sent to the user upon confirmation.
 * @param context The `ReadWriteUser` instance representing the user confirming the action.
 * @param additionalOperations A lambda function containing additional operations to be performed within the transaction.
 * @return BotResult<*> The result of the confirmation operation, indicating success or failure.
 */
fun handleCommonConfirmation(
    bot: TelegramBot,
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
 * Handles common rejection actions for different bot commands. This function performs additional operations, logs the
 * rejection, sends a message to the user, deletes the user's data from the database, and updates the user's state to
 * idle. It uses a transaction to ensure database consistency.
 *
 * ## Usage:
 * This function is used to handle common rejection actions such as denying a request or revoking access. It
 * encapsulates the common logic required for these operations and allows for specific additional operations to be
 * performed within the transaction.
 *
 * ### Example 1: Using handleCommonRejection for Revocation
 * ```kotlin
 * fun handleRevocationRejection(bot: TelegramBot, context: ReadWriteUser): BotResult<*> {
 *     return handleCommonRejection(bot, "Your revocation request has been denied.", context) {
 *         // Additional operations for revocation rejection can be added here
 *     }
 * }
 * ```
 *
 * @param bot The `TelegramBot` instance used to send messages to the user.
 * @param message The message to be sent to the user upon rejection.
 * @param context The `ReadWriteUser` instance representing the user denying the action.
 * @param additionalOperations A lambda function containing additional operations to be performed within the
 *   transaction.
 * @return BotResult<*> The result of the rejection operation, indicating success or failure.
 */
fun handleCommonRejection(
    bot: TelegramBot,
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
