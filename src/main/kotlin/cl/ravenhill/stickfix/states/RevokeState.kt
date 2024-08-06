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

data class RevokeState(override val context: ReadWriteUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        context.state = this
    }

    override fun process(text: String?, bot: TelegramBot): BotResult {
        super.process(text, bot)
        val cleanText = text?.uppercase() ?: "INVALID"
        return when (cleanText) {
            "YES" -> handleConfirmation(bot)
            "NO" -> handleRejection(bot)
            else -> handleInvalidInput(bot, context)
        }
    }

    private fun handleConfirmation(bot: TelegramBot): BotResult = transaction {
        Users.deleteWhere { id eq context.userId }
        logger.info("User ${context.username} has been revoked.")
        bot.sendMessage(context, "Your registration has been revoked.")
        BotSuccess("Your registration has been revoked.")
    }

    private fun handleRejection(bot: TelegramBot): BotResult {
        logger.info("User ${context.username} has chosen not to revoke.")
        bot.sendMessage(context, "Your registration has not been revoked.")
        return BotSuccess("Your registration has not been revoked.")
    }
}
