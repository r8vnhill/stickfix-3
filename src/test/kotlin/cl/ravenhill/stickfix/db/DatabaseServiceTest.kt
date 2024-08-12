package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.matchers.shouldBeLeft
import cl.ravenhill.stickfix.matchers.shouldBeRight
import cl.ravenhill.stickfix.states.IdleState
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.PropTestListener
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction


@OptIn(ExperimentalKotest::class)
class DatabaseServiceTest : FreeSpec({
    lateinit var database: Database

    beforeEach {
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    "A DatabaseService" - {
        "when retrieving a user that does not exist in the database" - {
            "should return a DatabaseOperationFailure" - {
                "if the user ID is not present in the database" {
                    checkAll(
                        PropTestConfig(listeners = listOf(ResetTableListener(database))),
                        arbInitDatabaseWithUserIdNotInDatabase(database)
                    ) { userId ->
                        val result = DatabaseServiceImpl(database).getUser(userId)
                        with(
                            result.shouldBeLeft()
                                .leftOrNull()
                                .shouldNotBeNull()
                        ) {
                            message shouldBe "Database operation failed."
                            data.message shouldContain "User must be present in the database"
                        }
                    }
                }
            }
        }

        "when retrieving a user that exists in the database" - {
            "should return a DatabaseOperationSuccess" - {
                "if the user ID is present in the database" {
                    checkAll(
                        PropTestConfig(listeners = listOf(ResetTableListener(database))),
                        arbInitDatabaseWithUserIdInDatabase(database)
                    ) { userId ->
                        val result = DatabaseServiceImpl(database).getUser(userId)
                        result.shouldBeRight()
                            .rightOrNull()
                            .shouldNotBeNull()
                            .shouldBe(
                                DatabaseOperationSuccess(
                                    "Database operation completed successfully.",
                                    StickfixUser("user$userId", userId)
                                )
                            )
                    }
                }
            }
        }
    }
})

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
private fun arbInitDatabaseWithUserIdNotInDatabase(database: Database) = arbitrary {
    val userIds = Arb.list(Arb.long(), 0..10).filter { it.distinct().size == it.size }.bind()
    transaction(database) {
        userIds.forEach { userId ->
            Users.insert {
                it[username] = "user$userId"
                it[chatId] = userId
                it[state] = IdleState::class.simpleName!!
            }
        }
    }
    Arb.long().filter { it !in userIds }.bind()
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
private fun arbInitDatabaseWithUserIdInDatabase(database: Database) = arbitrary { (random, _) ->
    val userIds = Arb.list(Arb.long(), 1..10).filter { it.distinct().size == it.size }.bind()
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
 * Returns the `Right` value of an `Either` if it exists, or `null` if the `Either` is `Left`.
 *
 * This extension function is used to extract the `Right` value from an `Either` if it is present, returning it
 * directly. If the `Either` is a `Left`, the function returns `null`.
 *
 * @receiver The `Either` instance from which to extract the `Right` value.
 * @return The `Right` value if present, or `null` if the `Either` is `Left`.
 */
private fun <A, B> Either<A, B>.rightOrNull(): B? = fold({ null }, { it })

/**
 * A listener for property-based testing that resets the `Users` table after each test.
 *
 * This listener drops the `Users` table and recreates it after each test to ensure that the table is in a clean state
 * before each test run. This helps to prevent data contamination across tests and ensures that each test starts with
 * a fresh database schema.
 *
 * @param database The `Database` instance where the `Users` table is reset.
 */
private class ResetTableListener(private val database: Database) : PropTestListener {

    override suspend fun afterTest() {
        transaction(database) {
            SchemaUtils.drop(Users)
            SchemaUtils.create(Users)
        }
    }
}

/**
 * Implementation of the `DatabaseService` interface that provides access to the database.
 *
 * This class implements the `DatabaseService` interface, which provides various operations for interacting with the
 * database. The implementation is specific to the given `Database` instance passed to the constructor.
 *
 * @property database The `Database` instance used to perform database operations.
 */
private class DatabaseServiceImpl(override val database: Database) : DatabaseService

