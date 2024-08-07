package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

/**
 * Represents the state for managing the private mode of a user in the bot. This class implements the `State` interface
 * and handles the processing of user input to enable or disable private mode.
 *
 * ## Usage:
 * This class should be instantiated with a `ReadWriteUser` context. Upon instantiation, it sets itself as the user's
 * current state. The `process` method handles the user's input to either enable or disable private mode, updating the
 * user's settings in the database accordingly.
 *
 * ### Example 1: Creating and Using PrivateModeState
 * ```kotlin
 * val user = ReadWriteUserImpl("username", 12345L)  // Assume ReadWriteUserImpl is an implementation of ReadWriteUser
 * val privateModeState = PrivateModeState(user)
 * val bot = TelegramBotImpl("your_bot_token")  // Assume TelegramBotImpl is an implementation of TelegramBot
 * privateModeState.process("ENABLE", bot)
 * ```
 *
 * @property context The `ReadWriteUser` instance representing the user associated with this state. This allows the
 *   state to access and modify user data as necessary.
 */
class PrivateModeState(override val context: ReadWriteUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        context.state = this
    }

    /**
     * Processes the user's input text and takes appropriate actions to enable or disable private mode.
     * If the input is invalid, it sends an error message to the user.
     *
     * @param text The input text provided by the user.
     * @param bot The `TelegramBot` instance used to send messages to the user.
     * @return BotResult<*> The result of processing the input, which could be enabling, disabling,
     *                      or an error message for invalid input.
     */
    override fun process(text: String?, bot: TelegramBot): BotResult<*> {
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
     * Handles enabling private mode by updating the user's settings in the database and sending a
     * confirmation message.
     *
     * @param bot The `TelegramBot` instance used to send messages to the user.
     * @return BotResult<*> The result of enabling private mode, indicating success or failure.
     */
    private fun handleEnable(bot: TelegramBot): BotResult<*> =
        handleCommonConfirmation(bot, "Private mode has been enabled.", context) {
            Users.update {
                it[privateMode] = true
            }
            logger.info("User ${context.username.ifBlank { context.userId.toString() }} enabled private mode")
        }

    /**
     * Handles disabling private mode by updating the user's settings in the database and sending a
     * confirmation message.
     *
     * @param bot The `TelegramBot` instance used to send messages to the user.
     * @return BotResult<*> The result of disabling private mode, indicating success or failure.
     */
    private fun handleDisable(bot: TelegramBot): BotResult<*> =
        handleCommonRejection(bot, "Private mode has been disabled.", context) {
            Users.update {
                it[privateMode] = false
            }
            logger.info("User ${context.username.ifBlank { context.userId.toString() }} disabled private mode")
        }
}
