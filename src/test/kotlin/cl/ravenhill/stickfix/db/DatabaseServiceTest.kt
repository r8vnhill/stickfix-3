package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.matchers.shouldBeLeft
import cl.ravenhill.stickfix.matchers.shouldBeRight
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.PrivateModeState
import cl.ravenhill.stickfix.states.RevokeState
import cl.ravenhill.stickfix.states.SealedState
import cl.ravenhill.stickfix.states.ShuffleState
import cl.ravenhill.stickfix.states.StartState
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.PropTestListener
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


@OptIn(ExperimentalKotest::class)
class DatabaseServiceTest : FreeSpec({
    lateinit var database: Database

    beforeEach {
        database = setupDatabase()
    }

    "A DatabaseService" - {
        "when retrieving a user" - {
            "should return a failure when the user does not exist in the database" {
                checkAll(
                    PropTestConfig(listeners = listOf(ResetTableListener(database))),
                    arbInitDatabaseWithUserIdNotInDatabase(database)
                ) { userId ->
                    val result = DatabaseServiceImpl(database).getUser(userId)

                    assertDatabaseOperationFailed(result, "User must be present in the database")
                }
            }

            "should return success when the user exists in the database" {
                checkAll(
                    PropTestConfig(listeners = listOf(ResetTableListener(database))),
                    arbInitDatabaseWithUserIdInDatabase(database)
                ) { userId ->
                    val result = DatabaseServiceImpl(database).getUser(userId)

                    assertDatabaseOperationSucceeded(
                        result,
                        StickfixUser("user$userId", userId)
                    )
                }
            }
        }

        "when adding a new user to the database" - {
            "should fail if the user already exists" {
                checkAll(
                    PropTestConfig(listeners = listOf(ResetTableListener(database))),
                    arbInitDatabaseWithUserIdInDatabase(database)
                ) { userId ->
                    val user = StickfixUser("user$userId", userId)
                    val result = DatabaseServiceImpl(database).addUser(user)

                    assertDatabaseOperationFailed(result, "User must not be present in the database")
                }
            }

            "should succeed if the user does not exist" {
                checkAll(
                    PropTestConfig(listeners = listOf(ResetTableListener(database))),
                    arbInitDatabaseWithUserIdNotInDatabase(database)
                ) { userId ->
                    val user = StickfixUser("user$userId", userId)
                    val result = DatabaseServiceImpl(database).addUser(user)

                    assertDatabaseOperationSucceeded(result, user)

                    verifyUserExistsInDatabase(database, userId, "user$userId")
                }
            }
        }

        "when setting a user state" - {
            "returns a DatabaseOperationFailure if the user does not exist" {
                checkAll(
                    PropTestConfig(listeners = listOf(ResetTableListener(database))),
                    arbInitDatabaseWithUserIdNotInDatabase(database),
                    arbStateBuilder()
                ) { userId, stateBuilder ->
                    val user = StickfixUser("user$userId", userId)
                    val result = DatabaseServiceImpl(database).setUserState(user, stateBuilder)

                    assertDatabaseOperationFailed(result, "User must be present in the database")
                }
            }
        }
    }
}) {
    companion object {
        private const val EXPECTED_FAILURE_MESSAGE = "Database operation failed."
        private const val EXPECTED_SUCCESS_MESSAGE = "Database operation completed successfully."

        /**
         * Sets up the database for testing by connecting to an in-memory H2 database and creating the necessary schema.
         *
         * @return The connected Database instance.
         */
        private fun setupDatabase(): Database {
            val database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
            transaction(database) {
                SchemaUtils.create(Users)
            }
            return database
        }

        /**
         * Asserts that the result of a database operation failed with the expected failure message.
         *
         * @param result The result of the database operation.
         * @param expectedErrorMessage The expected error message that should be contained in the failure.
         */
        private fun <T> assertDatabaseOperationFailed(
            result: Either<DatabaseOperationFailure, DatabaseOperationSuccess<T>>,
            expectedErrorMessage: String
        ) {
            result.shouldBeLeft()
                .leftOrNull()
                .shouldNotBeNull()
                .apply {
                    message shouldBe EXPECTED_FAILURE_MESSAGE
                    data.message shouldContain expectedErrorMessage
                }
        }

        /**
         * Asserts that the result of a database operation succeeded with the expected success message and user data.
         *
         * @param result The result of the database operation.
         * @param expectedUser The expected user data that should be returned in the success result.
         */
        private fun assertDatabaseOperationSucceeded(
            result: Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>>,
            expectedUser: StickfixUser
        ) {
            result.shouldBeRight()
                .rightOrNull()
                .shouldNotBeNull()
                .shouldBe(DatabaseOperationSuccess(EXPECTED_SUCCESS_MESSAGE, expectedUser))
        }

        /**
         * Verifies that a user with the specified ID exists in the database with the expected username.
         *
         * @param database The database instance to query.
         * @param userId The ID of the user to verify.
         * @param expectedUsername The expected username of the user.
         */
        private fun verifyUserExistsInDatabase(database: Database, userId: Long, expectedUsername: String) {
            transaction(database) {
                Users.selectAll().where { Users.chatId eq userId }.apply {
                    count() shouldBe 1
                    single().apply {
                        this[Users.username] shouldBe expectedUsername
                        this[Users.state] shouldBe IdleState::class.simpleName
                    }
                }
            }
        }
    }
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

private fun arbStateBuilder(): Arb<(StickfixUser) -> SealedState> = Arb.element(
    ::IdleState,
    ::PrivateModeState,
    ::RevokeState,
    ::ShuffleState,
    ::StartState
)

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

