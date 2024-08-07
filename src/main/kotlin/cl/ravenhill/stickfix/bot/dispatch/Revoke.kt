package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.RevokeCommand
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.error
import cl.ravenhill.stickfix.info
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("bot.dispatch.Revoke")

/**
 * Registers the revoke command within the given dispatcher context. This function handles the revocation process by
 * invoking the `RevokeCommand` and logging the results of the execution.
 *
 * @param databaseService The `StickfixDatabase` instance used for accessing and updating user data.
 * @param bot The `StickfixBot` instance used to send messages to the user.
 */
context(Dispatcher)
internal fun registerRevokeCommand(databaseService: StickfixDatabase, bot: StickfixBot) {
    command(RevokeCommand.NAME) {
        info(logger) { "Received revoke command" }
        when (val result = RevokeCommand(StickfixUser.from(message.from!!), bot, databaseService).execute()) {
            is CommandSuccess -> info(logger) { "Revoke command executed successfully: $result" }
            is CommandFailure -> error(logger) { "Revoke command failed: $result" }
        }
    }
}

/**
 * Registers the revoke confirmation callback for "Yes" responses within the given dispatcher context. This function
 * handles the confirmation process by invoking the `RevokeConfirmationYes` callback and logging the results of the
 * execution.
 *
 * @param databaseService The `StickfixDatabase` instance used for accessing and updating user data.
 */
context(Dispatcher)
internal fun registerRevokeConfirmationYes(databaseService: StickfixDatabase) {
    callbackQuery(RevokeConfirmationYes.name) {
        databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { error(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = { RevokeConfirmationYes(it.data, StickfixBot(databaseService), databaseService) }
        )
    }
}

/**
 * Registers the revoke confirmation callback for "No" responses within the given dispatcher context. This function
 * handles the rejection process by invoking the `RevokeConfirmationNo` callback and logging the results of the
 * execution.
 *
 * @param databaseService The `StickfixDatabase` instance used for accessing and updating user data.
 */
context(Dispatcher)
internal fun registerRevokeConfirmationNo(databaseService: StickfixDatabase) {
    callbackQuery(RevokeConfirmationNo.name) {
        databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { error(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = {
                RevokeConfirmationNo(it.data, StickfixBot(databaseService), databaseService)
            }
        )
    }
}
