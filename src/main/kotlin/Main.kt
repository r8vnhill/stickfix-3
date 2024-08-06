/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

import cl.ravenhill.stickfix.bot.LocalBot
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.commands.StartCommand
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.MapDatabaseService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

private val logger = LoggerFactory.getLogger("Main")

@OptIn(ExperimentalTime::class)
fun main() {
    val (databaseService, databaseInitTime) = initDatabase(MapDatabaseService())
    logger.info("Database initialized in $databaseInitTime")
    val bot = measureTimedValue {
        LocalBot(databaseService.apiToken)
    }
    logger.info("Bot setup in ${bot.duration}")
}

/**
 * Initializes a `DatabaseService` and measures the time taken to complete the initialization.
 * This function is marked as private and uses Kotlin's `measureTimedValue` function to perform
 * both actions simultaneously, which is helpful for performance monitoring.
 *
 * @param db
 *  The `DatabaseService` instance to be initialized.
 * @return
 *  A `TimedValue<DatabaseService>` containing the initialized database service and the time taken
 *  to initialize it, facilitating performance analysis.
 */
@OptIn(ExperimentalTime::class)
private fun initDatabase(db: DatabaseService): TimedValue<DatabaseService> {
    logger.info("Initializing database")
    return measureTimedValue {
        db.init()
    }
}

context(Bot.Builder)
fun registerCommands(databaseService: DatabaseService, bot: TelegramBot) {

}
