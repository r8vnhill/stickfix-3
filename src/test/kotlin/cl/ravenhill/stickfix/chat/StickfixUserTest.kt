/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class StickfixUserTest : FreeSpec(
    {
        "A StickfixUser" - {
            "when getting debug info" - {
                "returns the username if it is not blank" {
                    val user = StickfixUser("user", 1)
                    user.debugInfo shouldBe "user"
                }

                "returns the user ID if the username is blank" {
                    val user = StickfixUser("", 1)
                    user.debugInfo shouldBe "1"
                }
            }
        }
    })
