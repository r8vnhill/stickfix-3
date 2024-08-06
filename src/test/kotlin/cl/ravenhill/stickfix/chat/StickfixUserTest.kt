/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import com.github.kotlintelegrambot.entities.User
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class StickfixUserTest : FreeSpec({
    "A StickfixUser" - {
        "when getting debug info" - {
            "returns the username if it is not blank" {
                checkAll(Arb.string(1..100), Arb.long()) { username, userId ->
                    val user = StickfixUser(username, userId)
                    user.debugInfo shouldBe username
                }
            }

            "returns the user ID if the username is blank" {
                checkAll(Arb.long()) { userId ->
                    val user = StickfixUser("", userId)
                    user.debugInfo shouldBe userId.toString()
                }
            }
        }

        "can be created from a Telegram user" {
            checkAll(arbTelegramUser()) { telegramUser ->
                val user = StickfixUser.from(telegramUser)
                user.username shouldBe telegramUser.username
                user.userId shouldBe telegramUser.id
            }
        }
    }
})

private fun arbTelegramUser(): Arb<User> = arbitrary {
    User(
        id = Arb.long().bind(),
        isBot = Arb.boolean().bind(),
        supportsInlineQueries = Arb.boolean().bind(),
        lastName = Arb.string().bind(),
        firstName = Arb.string().bind(),
        username = Arb.string().bind(),
        languageCode = Arb.string().bind(),
        canJoinGroups = Arb.boolean().bind(),
        canReadAllGroupMessages = Arb.boolean().bind(),
    )
}

