package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.BotSuccess
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Represents the state where a user can confirm or deny the revocation of their registration in the Stickfix bot
 * application. This state allows the user to finalize their decision regarding the revocation process and handles the
 * appropriate transitions based on the user's input. The `RevokeState` class implements the `State` interface,
 * facilitating state-specific actions and transitions.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class RevokeState(override val user: StickfixUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Initialize the state by setting the user's current state to this state
    init {
        user.state = this
    }

    /**
     * Processes the user's input text and takes appropriate actions to confirm or deny the revocation. Provides
     * feedback for invalid inputs.
     *
     * @param text The input text provided by the user.
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of processing the input, indicating success or failure.
     */
    override fun process(text: String?, bot: StickfixBot): BotResult<*> {
        super.process(text, bot)
        val cleanText = text?.uppercase() ?: "INVALID"
        return when (cleanText) {
            "YES" -> handleConfirmation(bot)
            "NO" -> handleRejection(bot)
            else -> handleInvalidInput(
                bot,
                user,
                "Invalid input. Please type 'yes' or 'no' to confirm or deny revocation."
            )
        }
    }

    /**
     * Handles the confirmation of revocation, removing the user from the database and sending a confirmation message.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of confirming the revocation, indicating success or failure.
     */
    private fun handleConfirmation(bot: StickfixBot): BotResult<*> = transaction {
        Users.deleteWhere { id eq user.userId }
        logger.info("User ${user.username} has been revoked.")
        bot.sendMessage(user, "Your registration has been revoked.")
        BotSuccess("Your registration has been revoked.", true)
    }

    /**
     * Handles the rejection of revocation, retaining the user's registration and sending a confirmation message.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of rejecting the revocation, indicating success or failure.
     */
    private fun handleRejection(bot: StickfixBot): BotResult<*> {
        logger.info("User ${user.username} has chosen not to revoke.")
        bot.sendMessage(user, "Your registration has not been revoked.")
        return BotSuccess("Your registration has not been revoked.", true)
    }
}
