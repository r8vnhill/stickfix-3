package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

/**
 * Represents the state where a user can enable or disable private mode in the Stickfix bot application. This state
 * allows the user to change their privacy settings and handles the appropriate transitions based on the user's input.
 * The `PrivateModeState` class implements the `State` interface, facilitating state-specific actions and transitions.
 *
 * @property context A `ReadWriteUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
class PrivateModeState(override val context: ReadWriteUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        context.state = this
    }

    /**
     * Processes the user's input text and takes appropriate actions to enable or disable private mode. Provides
     * feedback for invalid inputs.
     *
     * @param text The input text provided by the user.
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of processing the input, indicating success or failure.
     */
    override fun process(text: String?, bot: StickfixBot): BotResult<*> {
        super.process(text, bot)
        return when (text?.uppercase()) {
            "ENABLE" -> handleEnable(bot)
            "DISABLE" -> handleDisable(bot)
            else -> handleInvalidInput(
                bot,
                context,
                "Invalid input. Please type 'enable' or 'disable' to change the private mode."
            )
        }
    }

    /**
     * Handles enabling private mode for the user and updates the database accordingly.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of enabling private mode, indicating success or failure.
     */
    private fun handleEnable(bot: StickfixBot): BotResult<*> =
        handleCommonConfirmation(bot, "Private mode has been enabled.", context) {
            Users.update {
                it[privateMode] = true
            }
            logger.info("User ${context.username.ifBlank { context.userId.toString() }} enabled private mode")
        }

    /**
     * Handles disabling private mode for the user and updates the database accordingly.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of disabling private mode, indicating success or failure.
     */
    private fun handleDisable(bot: StickfixBot): BotResult<*> =
        handleCommonRejection(bot, "Private mode has been disabled.", context) {
            Users.update {
                it[privateMode] = false
            }
            logger.info("User ${context.username.ifBlank { context.userId.toString() }} disabled private mode")
        }
}
