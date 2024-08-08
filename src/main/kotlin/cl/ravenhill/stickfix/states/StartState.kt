package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.slf4j.LoggerFactory


/**
 * Represents the state where a user can confirm or deny their registration in the Stickfix bot application. This state
 * allows the user to finalize their decision regarding the registration process and handles the appropriate transitions
 * based on the user's input. The `StartState` class implements the `State` interface, facilitating state-specific
 * actions and transitions.
 *
 * @property user A `ReadWriteUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class StartState(override val user: ReadWriteUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        user.state = this
    }

    /**
     * Processes the user's input text and takes appropriate actions to confirm or deny the registration. Provides
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
                "Invalid input. Please type 'yes' or 'no' to confirm or deny registration."
            )
        }
    }

    /**
     * Handles the confirmation of registration, logging the action and updating the database.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of confirming the registration, indicating success or failure.
     */
    private fun handleConfirmation(bot: StickfixBot): BotResult<*> =
        handleCommonConfirmation(bot, "You were successfully registered!", user) {
            logger.info("User ${user.username.ifBlank { user.userId.toString() }} confirmed start")
        }

    /**
     * Handles the rejection of registration, logging the action, deleting the user from the database, and sending a
     * confirmation message.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of rejecting the registration, indicating success or failure.
     */
    private fun handleRejection(bot: StickfixBot): BotResult<*> =
        handleCommonRejection(bot, "Registration cancelled.", user) {
            logger.info("User ${user.username.ifBlank { user.userId.toString() }} denied start")
            Users.deleteWhere { id eq user.userId }
        }

    /**
     * Provides a string representation of the state, returning the simple name of the class.
     *
     * @return String The simple name of the class.
     */
    override fun toString() = this::class.simpleName!!
}
