package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.RevokeCommand
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.db.schema.Users
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("bot.dispatch.Revoke")

context(Dispatcher)
internal fun registerRevokeCommand(databaseService: StickfixDatabase, bot: StickfixBot) {
    command(RevokeCommand.NAME) {
        logger.info("Received revoke command from ${message.from}")
        when (val result = RevokeCommand(StickfixUser.from(message.from!!), bot, databaseService).execute()) {
            is CommandSuccess -> logger.info("Revoke command executed successfully: $result")
            is CommandFailure -> logger.error("Revoke command failed: $result")
        }
    }
}

context(Dispatcher)
internal fun registerRevokeConfirmationYes(databaseService: StickfixDatabase) {
    callbackQuery(RevokeConfirmationYes.name) {
        val user = transaction {
            StickfixUser.from(databaseService.getUser(callbackQuery.from.id))
        }
        RevokeConfirmationYes.invoke(user, StickfixBot(databaseService), databaseService)
    }
}

context(Dispatcher)
internal fun registerRevokeConfirmationNo(databaseService: StickfixDatabase, bot: StickfixBot) {
    callbackQuery(RevokeConfirmationNo.name) {
        val user = transaction {
            StickfixUser.from(Users.selectAll().where { Users.id eq callbackQuery.from.id }.single())
        }
        RevokeConfirmationNo.invoke(user, StickfixBot(databaseService), databaseService)
    }
}
