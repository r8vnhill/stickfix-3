/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class BotFailureTest : FreeSpec(
    {
        "A BotFailure" - {
            "should have a message" {
                checkAll<String> { message ->
                    val botFailure = BotFailure(message)
                    botFailure.message shouldBe message
                }
            }
        }
    })
