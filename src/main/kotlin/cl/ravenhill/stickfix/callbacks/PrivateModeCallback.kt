package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService

sealed class PrivateModeCallback : CallbackQueryHandler()

data object PrivateModeEnabled : PrivateModeCallback() {
    override val name: String = this::class.simpleName!!
    override fun invoke(user: ReadUser, bot: TelegramBot, dbService: DatabaseService): CallbackResult {
        TODO("Not yet implemented")
    }
}
