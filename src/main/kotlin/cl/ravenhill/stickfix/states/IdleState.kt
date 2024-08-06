/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadWriteUser

class IdleState(override val context: ReadWriteUser) : State {
    init {
        context.state = this
    }

    override fun onStart(bot: TelegramBot): TransitionResult {
        TODO()
    }
}
