package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.DRIVER_NAME
import cl.ravenhill.stickfix.JDBC_URL
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.bot.arbStickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.TransitionSuccess
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbs.Username
import io.kotest.property.arbs.usernames
import io.kotest.property.checkAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class IdleTransitionImplTest : FreeSpec({

    "An IdleTransitionImpl" - {
        "should transition the user to the idle state" {
//            checkAll(arbStickfixDatabaseAndUser()) { (database, user) ->
//                with(StickfixBot(database)) {
//                    IdleTransitionImpl(user).onIdle().let { result ->
//                        user.state.shouldBeInstanceOf<IdleState>()
//                        result shouldBe TransitionSuccess(user.state)
//                    }
//                }
//            }
        }
    }
})

private fun arbStickfixBotAndUser() = arbitrary {
    val bot = arbStickfixBot().bind()
}

private fun arbUser(username: Arb<Username>, id: Arb<Long>) = Arb.bind(username, id) { username, id ->
    StickfixUser(username.value, id)
}
