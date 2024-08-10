package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixGroup
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.commands.CommandSuccess
import cl.ravenhill.stickfix.commands.HelpCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Help")

/**
 * Registers the `Help` command for the Stickfix bot within the specified dispatcher. This function uses the
 * `registerCommand` utility to bind the `HelpCommand` to its corresponding handler, enabling users to access help
 * information through the bot.
 *
 * ## Context:
 * This function operates within the context of the `StickfixBot` and `Dispatcher`, ensuring that the bot's commands
 * are properly registered and executed within the Telegram framework.
 *
 * @receiver StickfixBot The bot instance that handles the execution of commands and interaction with the Telegram API.
 * @receiver Dispatcher The dispatcher responsible for routing commands and callback queries within the bot.
 */
context(StickfixBot, Dispatcher)
internal fun registerHelpCommand() {
    command(HelpCommand.name) {
        // Extract the user from the message
        val chat = StickfixGroup.from(message.chat)
        // Log the received command and execute it
        logInfo(logger) { "Received ${HelpCommand.name} command from ${chat.debugInfo}" }
        when (val result = HelpCommand(chat)) {
            is CommandSuccess -> logInfo(logger) { "${HelpCommand.name} command executed successfully: $result" }
            is CommandFailure -> logError(logger) { "${HelpCommand.name} command failed: $result" }
        }
    }
}
