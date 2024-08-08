package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.StartCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Start")

/**
 * Registers the start confirmation callback for "Yes" responses within the given dispatcher context.
 * This function handles the confirmation process by invoking the `StartConfirmationYes` callback
 * and creating a `StickfixUser` from the callback query data.
 *
 * @param bot The `StickfixBot` instance used to send messages to the user and access the database service.
 */
context(Dispatcher)
internal fun registerStartConfirmationYes(bot: StickfixBot) {
    callbackQuery(StartConfirmationYes.name) {
        val user = StickfixUser.from(callbackQuery.from)
        StartConfirmationYes(user, bot)
    }
}

/**
 * Registers the start confirmation callback for "No" responses within the given dispatcher context.
 * This function handles the rejection process by invoking the `StartConfirmationNo` callback
 * and creating a `StickfixUser` from the callback query data.
 *
 * @param bot The `StickfixBot` instance used to send messages to the user and access the database service.
 */
context(Dispatcher)
internal fun registerStartConfirmationNo(bot: StickfixBot) {
    callbackQuery(StartConfirmationNo.name) {
        val user = StickfixUser.from(callbackQuery.from)
        StartConfirmationNo(user, bot)
    }
}

/**
 * Registers the start command within the given dispatcher context. This function handles the start
 * command by invoking the `StartCommand` and logging the results of the execution.
 *
 * @param bot The `StickfixBot` instance used to send messages to the user and access the database service.
 */
context(Dispatcher)
internal fun registerStartCommand(bot: StickfixBot) {
    command(StartCommand.NAME) {
        logInfo(logger) { "Received start command from ${message.from}" }
        when (val result = StartCommand(StickfixUser.from(message.from!!), bot).execute()) {
            is CommandSuccess -> logInfo(logger) { "Start command executed successfully: $result" }
            is CommandFailure -> logError(logger) { "Start command failed: $result" }
        }
    }
}
