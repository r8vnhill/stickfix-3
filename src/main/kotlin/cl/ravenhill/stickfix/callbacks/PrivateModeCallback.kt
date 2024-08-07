package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.StickfixDatabase

sealed class PrivateModeCallback : CallbackQueryHandler()

data object PrivateModeEnabled : PrivateModeCallback() {
    override val name: String = this::class.simpleName!!
    override fun invoke(user: ReadUser, bot: StickfixBot, databaseService: StickfixDatabase): CallbackResult {
        TODO("Not yet implemented")
    }
}
