/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import com.github.kotlintelegrambot.bot

class StickfixBot(override val token: String) : TelegramBot {
    private var started: Boolean = false

    private val _bot = bot {
        this@bot.token = this@StickfixBot.token
    }

    override fun start(): String = if (started) {
        "Bot already started"
    } else {
        started = true
        _bot.startPolling()
        "Bot started"
    }
}