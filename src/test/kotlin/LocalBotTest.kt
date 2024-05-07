import bot.LocalBot
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

class LocalBotTest : FreeSpec(
    {
        "A LocalBot" - {
            "can be created with a token" {
                checkAll<String> { token ->
                    val bot = LocalBot(token)
                    bot.token shouldBe token
                }
            }

            "can be started" {
                checkAll<String> { token ->
                    val bot = LocalBot(token)
                    bot.start() shouldBe "Bot started"
                    bot.start() shouldBe "Bot already started"
                }
            }
        }
    }
)