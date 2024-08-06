package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.BotSuccess
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Represents a state in which the user can confirm or reject the revocation of their registration. This state is part
 * of the state-driven application, handling the user's input to either revoke or retain their registration.
 *
 * ## Usage:
 * This class should be instantiated with a `ReadWriteUser` context. Upon instantiation, it sets itself as the user's
 * current state. The `process` method handles the user's response, guiding them through the revocation process based on
 * their input.
 *
 * ### Example 1: Creating and Using RevokeState
 * ```kotlin
 * val user = ReadWriteUserImpl("username", 12345L)  // Assume ReadWriteUserImpl is an implementation of ReadWriteUser
 * val revokeState = RevokeState(user)
 * val bot = TelegramBotImpl("your_bot_token")  // Assume TelegramBotImpl is an implementation of TelegramBot
 * revokeState.process("YES", bot)
 * ```
 *
 * @property context The `ReadWriteUser` instance representing the user associated with this state.
 *   This allows the state to access and modify user data during the revocation process.
 */
data class RevokeState(override val context: ReadWriteUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Initialize the state by setting the user's current state to this state
    init {
        context.state = this
    }

    /**
     * Processes the user's input text and takes appropriate actions based on the input. The input is expected to be
     * either "YES" to confirm revocation or "NO" to reject revocation. Any other input is considered invalid and will
     * prompt the user to provide valid input.
     *
     * @param text The input text provided by the user.
     * @param bot The `TelegramBot` instance used to send messages to the user.
     * @return BotResult The result of processing the input, which could be a confirmation, rejection, or an invalid
     *   input response.
     */
    override fun process(text: String?, bot: TelegramBot): BotResult {
        super.process(text, bot)
        val cleanText = text?.uppercase() ?: "INVALID"
        return when (cleanText) {
            "YES" -> handleConfirmation(bot)
            "NO" -> handleRejection(bot)
            else -> handleInvalidInput(bot, context)
        }
    }

    // Handles the confirmation of revocation by deleting the user from the database and notifying them
    private fun handleConfirmation(bot: TelegramBot): BotResult = transaction {
        Users.deleteWhere { id eq context.userId }
        logger.info("User ${context.username} has been revoked.")
        bot.sendMessage(context, "Your registration has been revoked.")
        BotSuccess("Your registration has been revoked.")
    }

    // Handles the rejection of revocation by notifying the user that their registration remains active
    private fun handleRejection(bot: TelegramBot): BotResult {
        logger.info("User ${context.username} has chosen not to revoke.")
        bot.sendMessage(context, "Your registration has not been revoked.")
        return BotSuccess("Your registration has not been revoked.")
    }
}
