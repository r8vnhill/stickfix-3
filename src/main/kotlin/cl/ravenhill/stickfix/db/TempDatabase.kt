package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Represents a temporary in-memory database for managing user data. This class initializes an H2 in-memory database and
 * provides functionality to periodically clean up old user entries based on a specified time threshold.
 *
 * @property logger The logger instance used for logging information and errors related to database operations.
 * @property database The in-memory H2 database instance. This property is lazily initialized during the `init` method.
 */
class TempDatabase : DatabaseService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override lateinit var database: Database
        private set

    /**
     * Initializes the in-memory H2 database and creates the necessary tables. This method returns a result indicating
     * whether the initialization was successful or if it failed due to a database operation error.
     *
     * @return Either a `DatabaseOperationFailure` if the initialization failed, or a `DatabaseOperationSuccess`
     *   containing the initialized `TempDatabase` instance if successful.
     */
    fun init(): Either<DatabaseOperationFailure, DatabaseOperationSuccess<TempDatabase>> {
        database = Database.connect("jdbc:h2:mem:temp;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
        return executeDatabaseOperationSafely(database) {
            SchemaUtils.create(Users)
            startCleanupJob(1.hours, 15.minutes)    // Cleanup users older than 1 hour every 15 minutes
            this@TempDatabase
        }
    }

    /**
     * Removes user entries older than the specified threshold from the database. This method returns a result
     * indicating whether the cleanup operation was successful or if it failed due to a database operation error.
     *
     * @param threshold The duration threshold for determining which users to delete. Entries older than this duration
     *   will be removed.
     * @return Either a `DatabaseOperationFailure` if the cleanup operation failed, or a `DatabaseOperationSuccess`
     *   containing the number of deleted users if successful.
     */
    private fun cleanupUsers(threshold: Duration): Either<DatabaseOperationFailure, DatabaseOperationSuccess<Int>> {
        val cutoffTime = Clock.System.now() - threshold
        return executeDatabaseOperationSafely(database) {
            Users.deleteWhere { created lessEq cutoffTime.toLocalDateTime(TimeZone.currentSystemDefault()) }
        }
    }

    /**
     * Starts a periodic cleanup job that removes user entries older than the specified threshold at a fixed interval.
     * This method uses coroutines to run the cleanup job asynchronously, logging the results of each cleanup operation.
     *
     * @param threshold The duration threshold for determining which users to delete. Entries older than this duration
     *   will be removed.
     * @param interval The interval at which the cleanup job should run. The cleanup operation will be executed every
     *   `interval` duration.
     */
    private fun startCleanupJob(threshold: Duration, interval: Duration) {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(interval.inWholeMilliseconds)
                cleanupUsers(threshold).fold(
                    ifLeft = { logError(logger) { "Failed to clean up users: ${it.data}" } },
                    ifRight = { if (it.data > 0) logInfo(logger) { "Cleaned up ${it.data} users." } }
                )
            }
        }
    }
}
