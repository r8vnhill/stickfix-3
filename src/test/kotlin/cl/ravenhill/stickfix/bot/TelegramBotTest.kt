/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import cl.ravenhill.stickfix.arbStickfixUser
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class TelegramBotTest : FreeSpec(
    {
        "A TelegramBot" - {
            "returns a success when a message is sent" {
                checkAll(arbStickfixUser(), Arb.string()) { user, message ->
                    TelegramBot.messageSentTo(user, message) shouldBe
                      BotSuccess("Message sent to ${user.debugInfo}: $message")
                }
            }
        }
    })