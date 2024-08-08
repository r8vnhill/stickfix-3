package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotFailure
import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.BotSuccess
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
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
 * @param user The `StickfixUser` instance representing the user who confirmed the action.
 * @param additionalOperations The additional operations to be performed within a database transaction.
 * @return BotResult<*> The result of the confirmation handling, indicating success or failure.
 */
fun handleCommonConfirmation(
    bot: StickfixBot,
    message: String,
    user: StickfixUser,
    additionalOperations: Transaction.() -> Unit,
): BotResult<*> = transaction {
    additionalOperations()
    info(logger) { "User ${user.debugInfo} confirmed action" }
    bot.sendMessage(user, message).fold(
        ifLeft = {
            BotFailure("Failed to send confirmation message", it)
        },
        ifRight = {
            user.onIdle(bot)
            BotSuccess("Confirmation message sent", it)
        }
    )
}

/**
 * Handles common rejection actions within the Stickfix bot application, performing additional operations,
 * sending rejection messages, and setting the user's state to idle.
 *
 * @param bot The `StickfixBot` instance used to send messages to the user.
 * @param message The rejection message to be sent to the user.
 * @param user The `StickfixUser` instance representing the user who denied the action.
 * @param additionalOperations The additional operations to be performed within a database transaction.
 * @return BotResult<*> The result of the rejection handling, indicating success or failure.
 */
fun handleCommonRejection(
    bot: StickfixBot,
    message: String,
    user: StickfixUser,
    additionalOperations: Transaction.() -> Unit,
): BotResult<*> = transaction {
    additionalOperations()
    info(logger) { "User ${user.debugInfo} denied action" }
    bot.sendMessage(user, message).also {
        user.onIdle(bot)
        verifyUserDeletion(it.flatten(), user)
    }.flatten()
}
