package cl.ravenhill.stickfix/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.db.StickfixDatabase
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private val logger = LoggerFactory.getLogger("Main")
private const val JDBC_URL = "jdbc:h2:file:./build/stickfix"
private const val JDBC_DRIVER = "org.h2.Driver"

/**
 * The main entry point for initializing and starting the Stickfix bot application. This function sets up the database
 * and the bot, measuring the time taken for each operation, and logs the results.
 */
@OptIn(ExperimentalTime::class)
fun main() {
    with(TimeSource.Monotonic) {
        val (databaseService, dbInitTime) = timed {
            initDatabase(StickfixDatabase(JDBC_URL, JDBC_DRIVER))
        }
        logInfo(logger) { "Database initialized in $dbInitTime ms" }
        val (bot, botSetupTime) = timed {
            StickfixBot(databaseService)
        }
        logInfo(logger) { "Bot setup in $botSetupTime ms" }
        bot.start().also { logInfo(logger) { it } }
    }
}

/**
 * Initializes the Stickfix database and handles any initialization errors by logging them and rethrowing the
 * exceptions. This function ensures that the database is properly set up before the bot starts.
 *
 * @param db The `StickfixDatabase` instance to be initialized.
 * @return The initialized `StickfixDatabase` instance.
 */
private fun initDatabase(db: StickfixDatabase): StickfixDatabase {
    logger.info("Initializing database")
    return db.init().fold(
        ifLeft = {
            logError(logger) { "Failed to initialize database: $it" }
            throw it.data
        },
        ifRight = { it.data }
    )
}

/**
 * Measures the time taken to execute a given block of code. This function uses the `TimeSource` context
 * to measure the duration of the block execution.
 *
 * @param T The return type of the block.
 * @param block The block of code to be executed and timed.
 * @return A pair containing the result of the block execution and the time taken in milliseconds.
 */
context(TimeSource)
@OptIn(ExperimentalTime::class)
private fun <T> timed(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val end = System.currentTimeMillis()
    return result to (end - start)
}
