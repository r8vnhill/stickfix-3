/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.bot

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class BotSuccessTest : FreeSpec(
    {
        "A BotSuccess" - {
            "should have a message" {
                checkAll<String> {
                    val botSuccess = BotSuccess(it)
                    botSuccess.message shouldBe it
                }
            }
        }
    })
