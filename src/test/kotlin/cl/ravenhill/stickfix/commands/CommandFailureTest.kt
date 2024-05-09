/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.commands.CommandFailure
import cl.ravenhill.stickfix.states.State
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.arbs.usernames
import io.kotest.property.checkAll

class CommandFailureTest : FreeSpec(
    {
        "A CommandFailure" - {
            "should have a user and a message" {
                checkAll(Arb.usernames(), Arb.long(), Arb.string()) { username, userId, message ->
                    val user = object : ReadUser {
                        override val username: String
                            get() = username.value
                        override val userId: Long
                            get() = userId
                        override val state: State
                            get() = TODO("Not yet implemented")
                    }
                    val commandSuccess = CommandFailure(user, message)
                    commandSuccess.user shouldBe user
                    commandSuccess.message shouldBe message
                }
            }
        }
    })
