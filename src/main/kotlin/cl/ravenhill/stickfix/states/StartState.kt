package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.slf4j.LoggerFactory

/**
 * Represents the state of a user actively engaging in the registration process within the Telegram bot system.
 * This state handles responses to registration prompts, managing transitions between states based on user input,
 * and updating or validating user records in the database.
 *
 * ## Usage:
 * This class is primarily used during the registration workflow to interpret user responses and guide them through
 * the necessary steps of registration. Depending on the user's input, it can confirm registration, reject it,
 * or request clarification in the case of unclear input.
 *
 * ### Example:
 * ```kotlin
 * val startState = StartState(user)
 * val result = startState.process("YES", bot)
 * println(result.message) // Expected to log a success or failure message based on user input.
 * ```
 *
 * @property context The context encapsulates the `ReadWriteUser` who is currently interacting with the bot.
 */
data class StartState(override val context: ReadWriteUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        context.state = this
    }

    /**
     * Processes the user's text input during the registration process and determines the next steps based
     * on the content of the input.
     *
     * @param text The user's input text, possibly null, indicating their decision during registration.
     * @param bot The Bot instance used to send messages to the user.
     * @return A `BotResult` indicating the outcome of the processing, including any state transitions or
     * necessary messages sent to the user.
     */
    override fun process(text: String?, bot: TelegramBot): BotResult<*> {
        super.process(text, bot)
        val cleanText = text?.uppercase() ?: "INVALID"
        return when (cleanText) {
            "YES" -> handleConfirmation(bot)
            "NO" -> handleRejection(bot)
            else -> handleInvalidInput(
                bot,
                context,
                "Invalid input. Please type 'yes' or 'no' to confirm or deny registration."
            )
        }
    }

    private fun handleConfirmation(bot: TelegramBot): BotResult<*> =
        handleCommonConfirmation(bot, "You were successfully registered!", context) {
            logger.info("User ${context.username.ifBlank { context.userId.toString() }} confirmed start")
        }

    private fun handleRejection(bot: TelegramBot): BotResult<*> =
        handleCommonRejection(bot, "Registration cancelled.", context) {
            logger.info("User ${context.username.ifBlank { context.userId.toString() }} denied start")
            Users.deleteWhere { id eq context.userId }
        }

    override fun toString() = this::class.simpleName!!
}
