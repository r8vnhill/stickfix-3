package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.bot.arbStickfixBotAndUser
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionResult
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.checkAll

class PrivateTransitionImplTest : FreeSpec({

    "A PrivateTransitionImpl" - {

        "should return a TransitionFailure when enabling private mode" {
            testTransitionFailure {
                with(it) {
                    onPrivateModeEnabled()
                }
            }
        }

        "should return a TransitionFailure when disabling private mode" {
            testTransitionFailure {
                with(it) {
                    onPrivateModeDisabled()
                }
            }
        }

        "should return a TransitionFailure when toggling private mode" {
            testTransitionFailure {
                with(it) {
                    onPrivateMode()
                }
            }
        }
    }
}) {
    companion object {

        // Helper function to test TransitionFailure for various private mode actions
        suspend fun testTransitionFailure(action: PrivateTransitionImpl.(StickfixBot) -> TransitionResult) {
            checkAll(arbStickfixBotAndUser()) { (bot, user) ->
                val result = PrivateTransitionImpl(user).action(bot)
                result.shouldBeInstanceOf<TransitionFailure>()
                result.nextState.shouldBeInstanceOf<PrivateTransitionImpl>()
            }
        }
    }
}
