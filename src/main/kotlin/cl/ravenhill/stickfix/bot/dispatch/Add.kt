package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixGroup
import cl.ravenhill.stickfix.commands.AddCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Help")

context(StickfixBot, Dispatcher)
internal fun registerAddCommand() {
    command(AddCommand.name) {
        // Extract the user from the message
        val chat = StickfixGroup.from(message.chat)
        return ReplySticker.from(message).fold(
            ifLeft = { error ->
                logError(logger) { "Failed to extract sticker from message: $error" }
            },
            ifRight = { sticker ->
                logInfo(logger) { "Received sticker from ${chat.debugInfo}: $sticker" }
                AddCommand(chat, sticker, args)
            }
        )
    }
}
