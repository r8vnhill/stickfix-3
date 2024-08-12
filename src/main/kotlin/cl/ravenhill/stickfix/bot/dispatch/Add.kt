package cl.ravenhill.stickfix.bot.dispatch

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.ReplySticker
import cl.ravenhill.stickfix.chat.StickfixGroup
import cl.ravenhill.stickfix.commands.AddCommand
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("bot.dispatch.Help")

/**
 * Registers the `/add` command within StickFix. This command allows users to add a sticker to a collection by replying
 * to a sticker message. The function extracts the necessary information from the incoming message, including the chat
 * and the sticker being replied to, and then executes the `AddCommand` to handle the sticker addition.
 *
 * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage user commands.
 * @receiver Dispatcher The dispatcher responsible for handling commands and routing them to the appropriate handlers.
 */
context(StickfixBot, Dispatcher)
internal fun registerAddCommand() {
    command(AddCommand.name) {
        // Extract the chat information from the message
        val chat = StickfixGroup.from(message.chat)

        // Attempt to extract the sticker from the replied-to message
        ReplySticker.from(message)?.let { sticker ->
            // Execute the add command with the extracted chat, sticker, and arguments
            AddCommand(chat, sticker, args)
        } ?: run {
            // Log an error and notify the user if the sticker extraction fails
            logError(logger) { "Failed to extract sticker from message" }
            sendMessage(chat, "Please reply to a sticker to add it to a collection").fold(
                ifLeft = { error ->
                    logError(logger) { "Failed to send message: $error" }
                },
                ifRight = { logInfo(logger) { "Message sent successfully" } }
            )
        }
    }
}
