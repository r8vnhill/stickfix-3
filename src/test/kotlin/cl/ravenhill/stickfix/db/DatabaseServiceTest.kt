package cl.ravenhill.stickfix.db

import arrow.core.Either
import cl.ravenhill.stickfix.ResetUsersTableListener
import cl.ravenhill.stickfix.STICKFIX_PUBLIC_ID
import cl.ravenhill.stickfix.STICKFIX_PUBLIC_USERNAME
import cl.ravenhill.stickfix.arbInitDatabaseWithUserIdInDatabase
import cl.ravenhill.stickfix.arbInitDatabaseWithUserIdNotInDatabase
import cl.ravenhill.stickfix.assertDatabaseOperationFailed
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.matchers.shouldBeRight
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.PrivateModeState
import cl.ravenhill.stickfix.states.RevokeState
import cl.ravenhill.stickfix.states.SealedState
import cl.ravenhill.stickfix.states.ShuffleState
import cl.ravenhill.stickfix.states.StartState
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


@OptIn(ExperimentalKotest::class)
class DatabaseServiceTest : FreeSpec({
    lateinit var database: Database

    beforeEach {
        database = setupDatabase()
    }

    "A DatabaseService" - {
        "retrieving a user" - {
            "should fail when the user does not exist" {
                testRetrievingNonExistentUser(database)
            }

            "should succeed when the user exists" {
                testRetrievingExistingUser(database)
            }
        }

        "adding a new user" - {
            "should fail if the user already exists" {
                testAddingExistingUser(database)
            }

            "should succeed if the user does not exist" {
                testAddingNonExistentUser(database)
            }
        }

        "setting a user state" - {
            "should fail if the user does not exist" {
                testSettingStateForNonExistentUser(database)
            }

            "should fail if the user is the public user" {
                testSetStateForDefaultUser(database)
            }

            "should succeed if the user exists" {
                testSettingStateForExistingUser(database)
            }
        }

        "deleting a user" - {
            "should fail if the user does not exist" {
                testDeletingNonExistentUser(database)
            }

            "should succeed if the user exists" {
                testDeletingExistingUser(database)
            }
        }

        "adding users should be symmetric to deleting users" {
            testSymmetricAddDelete(database)
        }
    }
}) {
    companion object {
        private const val EXPECTED_SUCCESS_MESSAGE = "Database operation completed successfully."

        /**
         * Sets up the in-memory H2 database for testing by connecting to it and creating the necessary schema.
         *
         * @return The connected Database instance, ready for transactions and queries.
         */
        private fun setupDatabase(): Database {
            val database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
            transaction(database) {
                SchemaUtils.drop(Users)
                SchemaUtils.create(Users)
            }
            return database
        }

        /**
         * Asserts that the result of a database operation succeeded with the expected success message and user data.
         *
         * @param result The result of the database operation.
         * @param expectedUser The expected user data that should be returned in the success result.
         */
        private fun assertDatabaseOperationSucceeded(
            result: Either<DatabaseOperationFailure, DatabaseOperationSuccess<StickfixUser>>,
            expectedUser: StickfixUser,
        ) {
            result.shouldBeRight()
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

        /**
         * Tests the retrieval of a non-existent user from the database, asserting that the operation fails with the
         * expected error message.
         *
         * @param database The database instance to use for the test.
         */
        private suspend fun testRetrievingNonExistentUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdNotInDatabase(database)
            ) { userId ->
                val result = DatabaseServiceImpl(database).getUser(userId)
                assertDatabaseOperationFailed(result, "User must be present in the database")
            }
        }

        /**
         * Tests the retrieval of an existing user from the database, asserting that the operation succeeds and returns the
         * expected user data.
         *
         * @param database The database instance to use for the test.
         */
        context(FreeSpecContainerScope)
        private suspend fun testRetrievingExistingUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdInDatabase(database)
            ) { userId ->
                val result = DatabaseServiceImpl(database).getUser(userId)
                assertDatabaseOperationSucceeded(result, StickfixUser("user$userId", userId))
            }
        }

        /**
         * Tests the addition of an existing user to the database, asserting that the operation fails with the expected
         * error message.
         *
         * @param database The database instance to use for the test.
         */
        context(FreeSpecContainerScope)
        private suspend fun testAddingExistingUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdInDatabase(database)
            ) { userId ->
                val user = StickfixUser("user$userId", userId)
                val result = DatabaseServiceImpl(database).addUser(user)
                assertDatabaseOperationFailed(result, "User must not be present in the database")
            }
        }

        /**
         * Tests the addition of a non-existent user to the database, asserting that the operation succeeds and the user
         * is added to the database.
         *
         * @param database The database instance to use for the test.
         */
        context(FreeSpecContainerScope)
        private suspend fun testAddingNonExistentUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdNotInDatabase(database)
            ) { userId ->
                val user = StickfixUser("user$userId", userId)
                val result = DatabaseServiceImpl(database).addUser(user)
                assertDatabaseOperationSucceeded(result, user)
                verifyUserExistsInDatabase(database, userId, "user$userId")
            }
        }

        /**
         * Tests setting the state for a non-existent user in the database, asserting that the operation fails with the expected
         * error message.
         *
         * @param database The database instance to use for the test.
         */
        context(FreeSpecContainerScope)
        private suspend fun testSettingStateForNonExistentUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdNotInDatabase(database),
                arbStateBuilder()
            ) { userId, stateBuilder ->
                val user = StickfixUser("user$userId", userId)
                val result = DatabaseServiceImpl(database).setUserState(user, stateBuilder)
                assertDatabaseOperationFailed(result, "User must be present in the database")
            }
        }

        /**
         * Tests setting the state for the public user in the database. This function verifies that attempting to set a
         * new state for the default public user (`STICKFIX_PUBLIC_USERNAME` with `STICKFIX_PUBLIC_ID`) results in a
         * failure, as the state of this user should not be modified.
         *
         * @param database The database instance used to perform the operation.
         */
        private suspend fun testSetStateForDefaultUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbStateBuilder()
            ) { stateBuilder ->
                val user = StickfixUser(STICKFIX_PUBLIC_USERNAME, STICKFIX_PUBLIC_ID)
                val result = DatabaseServiceImpl(database).setUserState(user, stateBuilder)
                assertDatabaseOperationFailed(result, "Cannot set user state of default user")
            }
        }

        /**
         * Tests setting the state for an existing user in the database, asserting that the operation succeeds and returns
         * the updated state.
         *
         * @param database The database instance to use for the test.
         */
        context(FreeSpecContainerScope)
        private suspend fun testSettingStateForExistingUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdInDatabase(database),
                arbStateBuilder()
            ) { userId, stateBuilder ->
                val user = StickfixUser("user$userId", userId)
                val result = DatabaseServiceImpl(database).setUserState(user, stateBuilder)
                result.shouldBeRight()
                    .shouldNotBeNull()
                    .apply {
                        this.data shouldBe stateBuilder(user)
                    }
            }
        }

        /**
         * Tests the deletion of a non-existent user from the database, asserting that the operation fails with the
         * expected error message.
         *
         * @param database The database instance to use for the test.
         */
        context(FreeSpecContainerScope)
        private suspend fun testDeletingNonExistentUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdNotInDatabase(database)
            ) { userId ->
                val user = StickfixUser("user$userId", userId)
                val result = DatabaseServiceImpl(database).deleteUser(user)
                assertDatabaseOperationFailed(result, "User must be present in the database")
            }
        }

        /**
         * Tests the deletion of an existing user from the database, asserting that the operation succeeds and the user
         * is removed from the database.
         *
         * @param database The database instance to use for the test.
         */
        context(FreeSpecContainerScope)
        private suspend fun testDeletingExistingUser(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                arbInitDatabaseWithUserIdInDatabase(database)
            ) { userId ->
                val user = StickfixUser("user$userId", userId)
                val result = DatabaseServiceImpl(database).deleteUser(user)
                assertDatabaseOperationSucceeded(result, user)
                transaction(database) {
                    Users.selectAll().where { Users.chatId eq userId }.count() shouldBe 0
                }
            }
        }

        /**
         * Tests the symmetric behavior of adding and then deleting users in the database. This function checks that
         * after you add a list of users to the database and subsequently delete them, the database remains empty.
         *
         * The test follows these steps:
         * 1. Generate a list of `StickfixUser` objects.
         * 2. Add each user in the list to the database using `DatabaseServiceImpl`.
         * 3. Delete each user in the list from the database.
         * 4. After deleting all users, assert that the `Users` table is empty.
         *
         * This test ensures that the `addUser` and `deleteUser` operations function correctly and symmetrically,
         * leaving the database in a clean state after performing these operations.
         *
         * @param database The database instance used to perform the operations.
         */
        context(FreeSpecContainerScope)
        private suspend fun testSymmetricAddDelete(database: Database) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database))),
                Arb.list(Arb.long().filter { it != 0L }.map { StickfixUser("user$it", it) }, 1..10)
            ) { users ->
                val databaseService = DatabaseServiceImpl(database)
                users.forEach(databaseService::addUser)
                users.forEach(databaseService::deleteUser)
                transaction(database) {
                    Users.selectAll().count() shouldBe 0
                }
            }
        }
    }
}

/**
 * Generates an arbitrary state builder function for a `StickfixUser`. This function returns an `Arb` instance that
 * randomly selects one of the provided state constructor functions. The selected state constructor function takes a
 * `StickfixUser` as input and returns an instance of a `SealedState` subclass.
 *
 * The available state constructor functions include:
 * - `IdleState`
 * - `PrivateModeState`
 * - `RevokeState`
 * - `ShuffleState`
 * - `StartState`
 *
 * This function is useful in property-based testing scenarios where you need to randomly generate different states for
 * a user in the `Stickfix` bot application.
 *
 * @return An `Arb` that generates a state constructor function for creating different states of a `StickfixUser`.
 */
private fun arbStateBuilder(): Arb<(StickfixUser) -> SealedState> = Arb.element(
    ::IdleState,
    ::PrivateModeState,
    ::RevokeState,
    ::ShuffleState,
    ::StartState
)

/**
 * Implementation of the `DatabaseService` interface that provides access to the database.
 *
 * This class implements the `DatabaseService` interface, which provides various operations for interacting with the
 * database. The implementation is specific to the given `Database` instance passed to the constructor.
 *
 * @property database The `Database` instance used to perform database operations.
 */
private class DatabaseServiceImpl(override val database: Database) : DatabaseService
