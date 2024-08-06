package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.schema.Users
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes

data class RevokeCommand(
    override val user: ReadUser,
    override val bot: TelegramBot,
    override val databaseService: DatabaseService
) : Command {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun execute(): CommandResult {
        logger.info("User ${user.username.ifBlank { user.userId.toString() }} revoked the bot")
        val result = transaction {
            if (Users.selectAll().where { Users.id eq user.userId }.count() == 0L) {
                bot.sendMessage(user, "User does not exist in the database, cannot revoke")
                CommandFailure(user, "User does not exist in the database, cannot revoke")
            } else {
                val message = "Are you sure you want to revoke your registration?"
                bot.sendMessage(user, message, replyMarkup = inlineKeyboardMarkup())
                user.onRevoke(bot)
                CommandSuccess(user, "Revoke command sent successfully")
            }
        }
        logger.info("Revoke command result: $result")
        return result
    }

    private fun inlineKeyboardMarkup() = InlineKeyboardMarkup.create(
        listOf(
            listOf(
                InlineKeyboardButton.CallbackData("Yes", RevokeConfirmationYes.name),
                InlineKeyboardButton.CallbackData("No", RevokeConfirmationNo.name)
            )
        )
    )

    companion object {
        const val NAME = "revoke"
    }
}
