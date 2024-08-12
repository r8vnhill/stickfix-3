package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.ReplySticker
import cl.ravenhill.stickfix.chat.StickfixChat
import cl.ravenhill.stickfix.commands.AddCommand.description
import cl.ravenhill.stickfix.commands.AddCommand.name
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo

/**
 * The `AddCommand` object represents the command used to add a sticker to a collection within StickFix. Users must
 * reply to a sticker message to use this command, allowing them to associate the sticker with specific tags and add
 * it to their collection.
 *
 * @property name The name of the command, which is "add".
 * @property description A brief description of the command, explaining that it adds a sticker to a collection and
 *   requires the user to reply to a sticker to use it.
 */
data object AddCommand : Command(
    name = "add",
    description = "Add a sticker to a collection. You must reply to a sticker to use this command."
) {

    /**
     * Handles the invocation of the add command within the StickFix application. This function processes the user's
     * request to add a sticker to a collection, sending a confirmation message and updating the database with the
     * sticker and associated tags.
     *
     * @param chat The `StickfixChat` instance representing the chat in which the command was invoked.
     * @param sticker The `ReplySticker` object representing the sticker that is being added to the collection.
     * @param tags A list of strings representing the tags associated with the sticker.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's stickers.
     * @return `CommandResult` indicating the result of the operation, which can be a success if the sticker is added
     *   successfully or a failure if there is an error during the process.
     */
    context(StickfixBot)
    operator fun invoke(chat: StickfixChat, sticker: ReplySticker, tags: List<String>) {
        logInfo(logger) { "Received sticker from ${chat.debugInfo}: $sticker" }
        when (databaseService.addSticker(chat, sticker, tags).fold(
            ifLeft = { error ->
                logError(logger) { "Failed to add sticker: $error" }
                CommandFailure(chat, "Failed to add sticker")
            },
            ifRight = { CommandSuccess(chat, "Sticker added successfully") }
        )) {
            is CommandFailure -> {
                return sendMessage(chat, "Failed to add sticker").fold(
                    ifLeft = { error ->
                        logError(logger) { "Failed to send message: $error" }
                    },
                    ifRight = { logInfo(logger) { "Message sent successfully" } }
                )
            }

            is CommandSuccess -> {
                logInfo(logger) { "Sticker added successfully" }
                sendMessage(chat, "Sticker added successfully to $tags")
            }
        }
    }
}
