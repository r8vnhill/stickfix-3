package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.CallbackFailure
import cl.ravenhill.stickfix.callbacks.CallbackSuccess
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.StartCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.logWarn
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Start")

/**
 * Registers the start confirmation callback for "Yes" responses within the given dispatcher context. This function
 * handles the confirmation process by invoking the `StartConfirmationYes` callback and creating a `StickfixUser` from
 * the callback query data.
 */
context(StickfixBot, Dispatcher)
internal fun registerStartConfirmationYes() {
    callbackQuery(StartConfirmationYes.name) {
        val user = StickfixUser.from(callbackQuery.from)
        when (val result = StartConfirmationYes(user)) {
            is CallbackFailure -> logError(logger) { "Failed to register user: $result" }
            is CallbackSuccess -> logInfo(logger) { "Registered new user: ${user.debugInfo}" }
        }
    }
}

/**
 * Registers the start confirmation callback for "No" responses within the given dispatcher context. This function
 * handles the rejection process by invoking the `StartConfirmationNo` callback and creating a `StickfixUser` from the
 * callback query data.
 */
context(StickfixBot, Dispatcher)
internal fun registerStartConfirmationNo() {
    callbackQuery(StartConfirmationNo.name) {
        val user = databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = {
                logWarn(logger) { "Failed to retrieve user data: $it" }
                StickfixUser.from(callbackQuery.from)
            },
            ifRight = {
                logInfo(logger) { "Retrieved user data: $it" }
                it.data
            }
        )
        StartConfirmationNo(user)
    }
}

/**
 * Registers the start command within the given dispatcher context. This function handles the start
 * command by invoking the `StartCommand` and logging the results of the execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerStartCommand() {
    command(StartCommand.name) {
        logInfo(logger) { "Received start command from ${message.from}" }
        when (val result = StartCommand(StickfixUser.from(message.from!!))) {
            is CommandSuccess -> logInfo(logger) { "Start command executed successfully: $result" }
            is CommandFailure -> logError(logger) { "Start command failed: $result" }
        }
    }
}
