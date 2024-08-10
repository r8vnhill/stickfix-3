package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.RevokeCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("bot.dispatch.Revoke")

/**
 * Registers the revoke command within the given dispatcher context. This function handles the revocation process by
 * invoking the `RevokeCommand` and logging the results of the execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerRevokeCommand() {
    command(RevokeCommand.NAME) {
        logInfo(logger) { "Received revoke command" }
        when (val result = RevokeCommand(StickfixUser.from(message.from!!))) {
            is CommandSuccess -> logInfo(logger) { "Revoke command executed successfully: $result" }
            is CommandFailure -> logError(logger) { "Revoke command failed: $result" }
        }
    }
}

/**
 * Registers the revoke confirmation callback for "Yes" responses within the given dispatcher context. This function
 * handles the confirmation process by invoking the `RevokeConfirmationYes` callback and logging the results of the
 * execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerRevokeConfirmationYes() {
    callbackQuery(RevokeConfirmationYes.name) {
        databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { logError(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = { RevokeConfirmationYes(it.data) }
        )
    }
}

/**
 * Registers the revoke confirmation callback for "No" responses within the given dispatcher context. This function
 * handles the rejection process by invoking the `RevokeConfirmationNo` callback and logging the results of the
 * execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerRevokeConfirmationNo() {
    callbackQuery(RevokeConfirmationNo.name) {
        databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { logError(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = { RevokeConfirmationNo(it.data) }
        )
    }
}
