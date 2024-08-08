package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.PrivateModeDisabledCallback
import cl.ravenhill.stickfix.callbacks.PrivateModeEnabledCallback
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Private")

/**
 * Registers the private mode enabled callback within the given dispatcher context. This function handles the enabling
 * of private mode by invoking the `PrivateModeEnabledCallback` and logging the results of the execution.
 *
 * @param bot The `StickfixBot` instance used to send messages to the user and access the database service.
 */
context(Dispatcher)
internal fun registerPrivateModeEnabledCallback(bot: StickfixBot) {
    callbackQuery(PrivateModeEnabledCallback.name) {
        logInfo(logger) { "Received private mode enabled callback" }
        bot.databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { logError(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = { PrivateModeEnabledCallback(it.data, bot) }
        )
    }
}

/**
 * Registers the private mode disabled callback within the given dispatcher context. This function handles the disabling
 * of private mode by invoking the `PrivateModeDisabledCallback` and logging the results of the execution.
 *
 * @param bot The `StickfixBot` instance used to send messages to the user and access the database service.
 */
context(Dispatcher)
internal fun registerPrivateModeDisabledCallback(bot: StickfixBot) {
    callbackQuery(PrivateModeDisabledCallback.name) {
        logInfo(logger) { "Received private mode disabled callback" }
        bot.databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { logError(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = { PrivateModeDisabledCallback(it.data, bot) }
        )
    }
}
