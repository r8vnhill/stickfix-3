/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.StickfixDatabase
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private val logger = LoggerFactory.getLogger("Main")
private const val JDBC_URL = "jdbc:h2:file:./build/stickfix"
private const val JDBC_DRIVER = "org.h2.Driver"

@OptIn(ExperimentalTime::class)
fun main() {
    with(TimeSource.Monotonic) {
        val (databaseService, dbInitTime) = timed {
            initDatabase(StickfixDatabase(JDBC_URL, JDBC_DRIVER))
        }
        logger.info("Database initialized in $dbInitTime")
        val (bot, botSetupTime) = timed {
            StickfixBot(databaseService)
        }
        logger.info("Bot setup in $botSetupTime ms")
        logger.info(bot.start())
    }
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
private fun initDatabase(db: StickfixDatabase): StickfixDatabase {
    logger.info("Initializing database")
    return db.init().data
}

/**
 * Executes a block of code and measures the time it takes to complete, using a `TimeSource` context. This function is
 * useful for performance monitoring and logging, providing both the result of the block and the duration it took to
 * execute.
 *
 * @param block The block of code to be executed and timed.
 * @return Pair<T, Duration> A pair containing the result of the block execution and the duration it took to execute.
 */
context(TimeSource)
@OptIn(ExperimentalTime::class)
private fun <T> timed(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val end = System.currentTimeMillis()
    return result to (end - start)
}
