package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.CallbackFailure
import cl.ravenhill.stickfix.callbacks.CallbackSuccess
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.RevokeCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.utils.registerCommand
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("bot.dispatch.Revoke")

/**
 * Registers the revoke command within the given dispatcher context. This function handles the revocation process by
 * invoking the `RevokeCommand` and logging the results of the execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerRevokeCommand() {
    registerCommand(RevokeCommand.name, logger) { RevokeCommand(it) }
}

/**
 * Registers the revoke confirmation callback for "Yes" responses within the given dispatcher context. This function
 * handles the confirmation process by invoking the `RevokeConfirmationYes` callback and logging the results of the
 * execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerRevokeConfirmationYes() {
    callbackQuery(RevokeConfirmationYes.name) {
        val user = StickfixUser.from(callbackQuery.from)
        logInfo(logger) { "Received revoke confirmation from ${user.debugInfo}" }
        when (val result = RevokeConfirmationYes(user)) {
            is CallbackFailure -> logError(logger) { "Failed to revoke user: $result" }
            is CallbackSuccess -> logInfo(logger) { "Revoked user: ${user.debugInfo}" }
        }
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
        val user = StickfixUser.from(callbackQuery.from)
        logInfo(logger) { "Received revoke rejection from ${user.debugInfo}" }
        RevokeConfirmationNo(user)
    }
}
