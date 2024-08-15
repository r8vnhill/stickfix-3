package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.bot.arbStickfixBotAndUser
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.TransitionSuccess
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.checkAll

class IdleTransitionImplTest : FreeSpec({

    "An IdleTransitionImpl" - {
        "should transition the user to the idle state" {
            checkAll(arbStickfixBotAndUser()) { (bot, user) ->
                with(bot) {
                    val result = IdleTransitionImpl(user).onIdle()
                    result.shouldBeInstanceOf<TransitionSuccess>()
                    result.nextState.shouldBeInstanceOf<IdleState>()
                }
            }
        }
    }
})

