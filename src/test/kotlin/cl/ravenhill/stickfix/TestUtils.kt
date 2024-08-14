package cl.ravenhill.stickfix

import arrow.core.Either
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.DatabaseOperationFailure
import cl.ravenhill.stickfix.db.DatabaseOperationSuccess
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.db.schema.Stickers
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.matchers.shouldBeLeft
import cl.ravenhill.stickfix.states.IdleState
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestListener
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

private const val EXPECTED_FAILURE_MESSAGE = "Database operation failed."
internal const val JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
internal fun jdbcUrl(databaseName: String) = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1"
internal const val DRIVER_NAME = "org.h2.Driver"
internal const val STICKFIX_DEFAULT_USER_ID = 0L
internal const val STICKFIX_USER_NAME_USERNAME = "STICKFIX_PUBLIC"
internal val stickfixDefaultUserState = ::IdleState

/**
 * Returns the `Right` value of an `Either` if it exists, or `null` if the `Either` is `Left`.
 *
 * This extension function is used to extract the `Right` value from an `Either` if it is present, returning it
 * directly. If the `Either` is a `Left`, the function returns `null`.
 *
 * @receiver The `Either` instance from which to extract the `Right` value.
 * @return The `Right` value if present, or `null` if the `Either` is `Left`.
 */
internal fun <A, B> Either<A, B>.rightOrNull(): B? = fold({ null }, { it })


/**
 * Asserts that the result of a database operation failed with the expected failure message.
 *
 * @param result The result of the database operation.
 * @param expectedErrorMessage The expected error message that should be contained in the failure.
 * @param T The type of the successful result, which is not used in this failure case.
 */
internal fun <T> assertDatabaseOperationFailed(
    result: Either<DatabaseOperationFailure, DatabaseOperationSuccess<T>>,
    expectedErrorMessage: String,
) {
    result.shouldBeLeft()
        .shouldNotBeNull()
        .apply {
            message shouldBe EXPECTED_FAILURE_MESSAGE
            data.message shouldContain expectedErrorMessage
        }
}


/**
 * Initializes the database with a set of user IDs and returns a randomly selected user ID from the database.
 *
 * This function generates a list of distinct user IDs, inserts them into the `Users` table of the provided database,
 * and then returns a randomly selected user ID from the inserted values. The randomness is controlled by the given
 * `Random` instance.
 *
 * @param database The database instance where the user IDs will be inserted.
 * @return A randomly selected user ID from the list of inserted user IDs.
 */
internal fun arbInitDatabaseWithUserIdInDatabase(database: Database) = arbitrary { (random, _) ->
    val userIds = Arb.list(Arb.long().filter { it != 0L }, 1..10)
        .filter { it.distinct().size == it.size }
        .bind()
    transaction(database) {
        userIds.forEach { userId ->
            Users.insert {
                it[username] = "user$userId"
                it[chatId] = userId
                it[state] = IdleState::class.simpleName!!
            }
        }
    }
    userIds.random(random)
}

/**
 * Generates an arbitrary list of user IDs, inserts them into the database, and then generates a user ID that is not
 * present in the database.
 *
 * This function first generates a list of distinct long integers representing user IDs. It then inserts each of these
 * user IDs into the `Users` table in the database, associating each with a username, chat ID, and setting the state
 * to `IdleState`. After all user IDs have been inserted, it generates and returns a new long integer that is not
 * included in the list of user IDs already in the database.
 *
 * @param database The `Database` instance where user IDs are inserted.
 * @return A new long integer representing a user ID that is not already present in the database.
 */
internal fun arbInitDatabaseWithUserIdNotInDatabase(database: Database) = arbitrary {
    val userIds = Arb.list(Arb.long().filter { it != 0L }, 0..10)
        .filter { it.distinct().size == it.size }
        .bind()
    transaction(database) {
        userIds.forEach { userId ->
            Users.insert {
                it[username] = "user$userId"
                it[chatId] = userId
                it[state] = IdleState::class.simpleName!!
            }
        }
    }
    Arb.long()
        .filter { it !in userIds }
        .filter { it != 0L }
        .bind()
}

/**
 * Generates an arbitrary `StickfixUser` that is not present in the given database. This function relies on an existing
 * arbitrary that initializes the database with users and maps it to produce a `StickfixUser` instance with the
 * specified ID that is not already present in the database.
 *
 * @param database The `Database` instance where the user should not be present.
 * @return An `Arb<StickfixUser>` that generates `StickfixUser` instances with IDs not present in the specified
 *   database.
 */
fun arbInitDatabaseWithUserNotInDatabase(database: Database): Arb<StickfixUser> =
    arbInitDatabaseWithUserIdNotInDatabase(database).map { StickfixUser("user$it", it) }

/**
 * Generates an arbitrary `StickfixUser` that is already present in the database. This function uses a previously
 * initialized list of user IDs in the database and maps them to corresponding `StickfixUser` instances.
 *
 * @param database The database instance where the users are stored.
 * @return An `Arb<StickfixUser>` representing a user that is already present in the database.
 */
fun arbInitDatabaseWithUserInDatabase(database: Database): Arb<StickfixUser> =
    arbInitDatabaseWithUserIdInDatabase(database).map { StickfixUser("user$it", it) }

/**
 * A listener for property-based testing that resets the `Users` table after each test.
 *
 * This listener drops the `Users` table and recreates it after each test to ensure that the table is in a clean state
 * before each test run. This helps to prevent data contamination across tests and ensures that each test starts with
 * a fresh database schema.
 *
 * @param database The `Database` instance where the `Users` table is reset.
 */
internal class ResetUsersTableListener(private val database: Database) : PropTestListener {

    /**
     * Resets the `Users` table after each test by dropping and recreating the table.
     */
    override suspend fun afterTest() {
        transaction(database) {
            SchemaUtils.drop(Stickers)
            SchemaUtils.drop(Users)
            SchemaUtils.create(Users)
            SchemaUtils.create(Stickers)
        }
    }
}


/**
 * A `PropTestListener` implementation that resets the `Meta` table in the `StickfixDatabase` before and after each
 * test.
 *
 * This class is used to ensure a clean state of the `Meta` table for each property-based test. Before each test, the
 * database is initialized, ensuring that the `Meta` table is created and ready for use. After each test, the `Meta`
 * table is dropped, removing any data that was inserted during the test. This prevents any leftover state from
 * affecting subsequent tests, ensuring test isolation and reliability.
 *
 * @property database The `StickfixDatabase` instance that contains the `Meta` table to be reset.
 */
internal class ResetMetaTableAfterEach(private val database: StickfixDatabase) : PropTestListener {

    /**
     * Initializes the `StickfixDatabase` before each test, ensuring that the `Meta` table is created and ready for use.
     */
    override suspend fun beforeTest() {
        database.init()
    }

    /**
     * Drops the `Meta` table after each test, removing any data that was inserted during the test. This ensures that
     * subsequent tests start with a clean state.
     */
    override suspend fun afterTest() = transaction(database.database) {
        SchemaUtils.drop(Meta)
    }
}

/**
 * Generates an arbitrary database instance of a specified type using a given name. This function provides a flexible
 * way to create different types of database objects by passing a lambda function that defines the creation logic. It
 * connects to a database using a randomly generated name and the specified driver.

 * @param name An arbitrary string generator that produces the name of the database. Defaults to generating alphanumeric
 *   strings of length 1 to 16.
 * @param createDatabase A lambda function that takes a `Database` instance as an input and returns an instance of type
 *   `T`. This function defines how the database object should be created.
 * @return An `Arb<T>` that generates instances of the specified database type.
 */
internal fun <T> arbDatabase(
    name: Arb<String> = Arb.string(1..16, Codepoint.az()),
    createDatabase: (Database) -> T,
): Arb<T> = name.map {
    createDatabase(Database.connect(jdbcUrl(it), DRIVER_NAME))
}
