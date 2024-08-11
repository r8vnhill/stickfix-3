package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.ShuffleDisabledCallback
import cl.ravenhill.stickfix.callbacks.ShuffleEnabledCallback
import cl.ravenhill.stickfix.commands.ShuffleCommand
import cl.ravenhill.stickfix.utils.registerCallback
import cl.ravenhill.stickfix.utils.registerCommand
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Shuffle")

/**
 * Registers the shuffle command within the Stickfix bot application. This function binds the `ShuffleCommand` to the
 * bot's command system, allowing users to enable shuffle mode for their stickers. The shuffle mode lets users shuffle
 * their stickers with each request, enhancing user interaction with the bot.
 *
 * @receiver StickfixBot The bot instance that interacts with the Telegram API and handles user commands.
 * @receiver Dispatcher The dispatcher instance that registers the command with the bot's command system.
 */
context(StickfixBot, Dispatcher)
internal fun registerShuffleCommand() = registerCommand(ShuffleCommand.name, logger) { ShuffleCommand(it) }

/**
 * Registers the callback query handler for enabling shuffle mode in the Stickfix bot. This function leverages the
 * `registerCallback` utility to associate the `ShuffleEnabledCallback` with its respective action. When the callback
 * is triggered, the bot retrieves the user from the database and invokes the `ShuffleEnabledCallback` with the
 * retrieved user data.
 */
context(StickfixBot, Dispatcher)
internal fun registerShuffleEnabledCallback() = registerCallback(ShuffleEnabledCallback.name, logger) {
    ShuffleEnabledCallback(it)
}

/**
 * Registers the callback query handler for disabling shuffle mode in the Stickfix bot. This function uses the
 * `callbackQuery` function to listen for the callback query associated with disabling shuffle mode. Upon receiving
 * the callback, the bot retrieves the user from the database and initializes the `ShuffleDisabledCallback` with the
 * user data.
 */
context(StickfixBot, Dispatcher)
internal fun registerShuffleDisabledCallback() = registerCallback(ShuffleDisabledCallback.name, logger) {
    ShuffleDisabledCallback(it)
}
