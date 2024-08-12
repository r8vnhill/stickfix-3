package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot

data object AddCommand : Command(name = "add", description = "Add a sticker to a collection") {
    operator fun invoke(chat: StickfixBot, sticker: ReplySticker, args: List<String>) {
        TODO()
    }
}