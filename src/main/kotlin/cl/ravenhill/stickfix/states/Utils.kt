package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import cl.ravenhill.stickfix.info
import cl.ravenhill.stickfix.utils.flatten
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("states.Utils")

/**
 * Handles common confirmation actions within the Stickfix bot application, performing additional operations,
 * sending confirmation messages, and setting the user's state to idle.
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
    additionalOperations()
    info(logger) { "User ${context.username.ifBlank { context.userId.toString() }} confirmed action" }
    bot.sendMessage(context, message).also { result ->
        context.onIdle(bot)
        verifyUserState(result.flatten(), IdleState::class.simpleName!!, context)
    }.flatten()
}

/**
 * Handles common rejection actions within the Stickfix bot application, performing additional operations,
 * sending rejection messages, and setting the user's state to idle.
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
    additionalOperations()
    info(logger) { "User ${context.username.ifBlank { context.userId.toString() }} denied action" }
    bot.sendMessage(context, message).also {
        context.onIdle(bot)
        verifyUserDeletion(it.flatten(), context)
    }.flatten()
}
