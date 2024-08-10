package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.PrivateModeDisabledCallback
import cl.ravenhill.stickfix.callbacks.PrivateModeEnabledCallback
import cl.ravenhill.stickfix.commands.PrivateModeCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.registerCommand
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Private")

/**
 * Registers the private mode command for the Stickfix bot. This function uses a generalized `registerCommand` function
 * to handle the registration and execution of the command. The private mode command enables or disables private mode
 * for the user, allowing them to control the visibility of their stickers.
 *
 * This function is called in the context of both the `StickfixBot` and `Dispatcher` to facilitate the interaction
 * between the bot and the command handling mechanism provided by the dispatcher.
 *
 * The `registerCommand` function is used to eliminate duplicate code by abstracting the command registration and
 * execution logic. The command's name is provided, along with the logger instance and the specific logic for executing
 * the private mode command.
 *
 * @receiver StickfixBot The bot instance that provides the necessary functionality for managing
 *   commands and interacting with the Telegram API.
 * @receiver Dispatcher The dispatcher responsible for routing commands to their corresponding
 *   handlers within the bot.
 */
context(StickfixBot, Dispatcher)
internal fun registerPrivateModeCommand() {
    registerCommand(PrivateModeCommand.name, logger) { PrivateModeCommand(it) }
}

/**
 * Registers the private mode enabled callback within the given dispatcher context. This function handles the enabling
 * of private mode by invoking the `PrivateModeEnabledCallback` and logging the results of the execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerPrivateModeEnabledCallback() {
    callbackQuery(PrivateModeEnabledCallback.name) {
        logInfo(logger) { "Received private mode enabled callback" }
        databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { logError(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = { PrivateModeEnabledCallback(it.data) }
        )
    }
}

/**
 * Registers the private mode disabled callback within the given dispatcher context. This function handles the disabling
 * of private mode by invoking the `PrivateModeDisabledCallback` and logging the results of the execution.
 */
context(StickfixBot, Dispatcher)
internal fun registerPrivateModeDisabledCallback() {
    callbackQuery(PrivateModeDisabledCallback.name) {
        logInfo(logger) { "Received private mode disabled callback" }
        databaseService.getUser(callbackQuery.from.id).fold(
            ifLeft = { logError(logger) { "Failed to retrieve user: ${it.message}" } },
            ifRight = { PrivateModeDisabledCallback(it.data) }
        )
    }
}
