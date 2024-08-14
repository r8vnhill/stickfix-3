package cl.ravenhill.stickfix.bot

import cl.ravenhill.stickfix.DRIVER_NAME
import cl.ravenhill.stickfix.JDBC_URL
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.db.schema.Meta
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class StickfixBotTest : FreeSpec({


})

fun arbStickfixBot() = arbStickfixDatabase().map { StickfixBot(it) }

private fun arbStickfixDatabase() = arbitrary {
    StickfixDatabase(JDBC_URL, DRIVER_NAME).apply {
        transaction(database) {
            SchemaUtils.drop(Meta)
        }
        init()
        transaction(database) {
            Meta.insert {
                it[key] = "API_KEY"
                it[value] = "API_VALUE"
            }
        }
    }
}
