package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.arbStickfixUser
import cl.ravenhill.stickfix.bot.arbBot
import cl.ravenhill.stickfix.db.MapDatabaseService
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class StartConfirmationYesTest : FreeSpec({

    "A StartConfirmationYes handler" - {
        "should have a name that is equal to the class name" {
            val handler = StartConfirmationYes
            handler.name shouldBe "StartConfirmationYes"
        }

        "when sending a message" - {
            "should return a success if the message is sent successfully" {
                checkAll(arbBot(), arbStickfixUser()) { bot, user ->
                    val handler = StartConfirmationYes
                    val result = handler(user, bot, MapDatabaseService())
                    result shouldBe CallbackSuccess("Message sent successfully.")
                }
            }
        }
    }
})
