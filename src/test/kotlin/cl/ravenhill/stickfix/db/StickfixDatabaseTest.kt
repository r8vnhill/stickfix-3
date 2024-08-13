package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.DRIVER_NAME
import cl.ravenhill.stickfix.JDBC_URL
import cl.ravenhill.stickfix.ResetUsersTableListener
import cl.ravenhill.stickfix.STICKFIX_PUBLIC_ID
import cl.ravenhill.stickfix.STICKFIX_PUBLIC_USERNAME
import cl.ravenhill.stickfix.arbInitDatabaseWithUserIdInDatabase
import cl.ravenhill.stickfix.arbInitDatabaseWithUserIdNotInDatabase
import cl.ravenhill.stickfix.assertDatabaseOperationFailed
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.matchers.shouldBeLeft
import cl.ravenhill.stickfix.matchers.shouldBeRight
import cl.ravenhill.stickfix.modes.PrivateMode
import cl.ravenhill.stickfix.modes.ShuffleMode
import cl.ravenhill.stickfix.stickfixDefaultUserState
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.PropTestListener
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.hex
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class StickfixDatabaseTest : FreeSpec({

    lateinit var database: StickfixDatabase

    beforeEach {
        database = StickfixDatabase(JDBC_URL, DRIVER_NAME)
        database.init()
    }

    "A StickfixDatabase" - {
        "should be able to initialize the database" {
            testDatabaseInitialization(database)
        }

        "when querying for the API key" - {
            "should return a DatabaseOperationFailure when the key is not found" {
                testMissingApiKey(database)
            }

            "should return a DatabaseOperationSuccess when the key is found" {
                testPresentApiKey(database)
            }
        }

        "when retrieving a user" - {
            "should return a DatabaseOperationFailure if the user is not found" {
                testRetrievingNonExistentUser(database)
            }

            "should return a DatabaseOperationSuccess if the user is found" {
                testRetrievingExistentUser(database)
            }
        }

        "when setting the private mode" - {
            "should return a DatabaseOperationFailure if" - {
                "trying to set the mode of the default user" {
                    testSettingPrivateModeDefaultUser(database)
                }

                "the user is not found" {
                    testSettingPrivateModeNonExistentUser(database)
                }
            }

            "should return a DatabaseOperationSuccess if the mode is set successfully" {
                testSettingPrivateModeExistentUser(database)
            }
        }

        "when setting shuffle mode" - {
            "should return a DatabaseOperationFailure if" - {
                "trying to set the mode of the default user" {
                    testSettingShuffleModeDefaultUser(database)
                }

                "the user is not found" {
                    testSettingShuffleModeNonExistentUser(database)
                }
            }

            "should return a DatabaseOperationSuccess if the mode is set successfully" {
                testSettingShuffleModeExistentUser(database)
            }
        }
    }
}) {
    companion object {

        /**
         * Tests the initialization of the StickfixDatabase and verifies that the default user is present and correctly
         * initialized. This function checks whether the database can be initialized without errors, and whether the
         * initial user, typically a public or default user, exists in the database with the expected properties.
         *
         * This function performs the following checks:
         * 1. Ensures the database initializes successfully and returns the expected database instance.
         * 2. Verifies that the public/default user exists in the database after initialization, checking:
         *    - The user ID matches the expected public/default user ID.
         *    - The username is correctly set to the expected public/default username.
         *    - The user's state is set to the expected default state.
         *
         * @param database The instance of StickfixDatabase to be tested.
         */
        fun testDatabaseInitialization(database: StickfixDatabase) {
            // Initialize the database and verify the operation was successful
            database.init()
                .shouldBeRight()
                .shouldNotBeNull()
                .data shouldBe database

            // Verify that the public/default user exists and has the correct properties
            database.getUser(STICKFIX_PUBLIC_ID)
                .shouldBeRight()
                .shouldNotBeNull()
                .data
                .apply {
                    id shouldBe STICKFIX_PUBLIC_ID
                    username shouldBe STICKFIX_PUBLIC_USERNAME
                    state shouldBe stickfixDefaultUserState(this)
                }
        }

        /**
         * Tests the behavior of the `queryApiKey` method in the `StickfixDatabase` when the API key is missing from the
         * database. This function checks whether the database returns a failure when the API key is not found in the
         * meta table. It asserts that the error message indicates the absence of the API key in the meta table.
         *
         * @param database The `StickfixDatabase` instance used to query the API key.
         */
        private fun testMissingApiKey(database: StickfixDatabase) {
            database.queryApiKey()
                .shouldBeLeft()
                .shouldNotBeNull()
                .data.message shouldContain "API_KEY must be present in meta table"
        }

        /**
         * Tests the presence of an API key in the `Meta` table of the `StickfixDatabase`. This function uses
         * property-based testing to verify that when an API key is inserted into the `Meta` table, it can be
         * successfully retrieved from the database. The test ensures that the `queryApiKey` method of the
         * `StickfixDatabase` class behaves correctly when the API key is present.
         *
         * @param database The `StickfixDatabase` instance used to interact with the database during the test.
         */
        private suspend fun testPresentApiKey(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetMetaTableAfterEach(database))),
                arbKey()
            ) { apiKey ->
                transaction(database.database) {
                    // Insert the generated API key into the Meta table
                    Meta.insert {
                        it[key] = "API_KEY"
                        it[value] = apiKey
                    }
                }

                // Query the API key from the database and verify it matches the inserted value
                database.queryApiKey()
                    .shouldBeRight()
                    .shouldNotBeNull()
                    .data shouldBe apiKey
            }
        }

        /**
         * Tests retrieving a user that does not exist in the Stickfix database. This function checks that the
         * `getUser` method correctly identifies when a user is not present in the database and returns a failure.
         *
         * @param database The StickfixDatabase instance to be tested.
         * @throws AssertionError If the user retrieval does not result in a failure or the failure message is incorrect.
         */
        private suspend fun testRetrievingNonExistentUser(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database.database))),
                arbInitDatabaseWithUserNotInDatabase(database.database)
            ) { user ->
                val result = database.getUser(user)
                assertDatabaseOperationFailed(result, "User must be present in the database")
            }
        }

        /**
         * Tests retrieving a user that exists in the Stickfix database. This function verifies that the
         * `getUser` method successfully retrieves the user data from the database.
         *
         * @param database The StickfixDatabase instance to be tested.
         * @throws AssertionError If the user retrieval does not result in a success or the returned user data is incorrect.
         */
        private suspend fun testRetrievingExistentUser(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database.database))),
                arbInitDatabaseWithUserInDatabase(database.database)
            ) { user ->
                database.getUser(user)
                    .shouldBeRight()
                    .shouldNotBeNull()
                    .data shouldBe user
            }
        }

        /**
         * Tests setting the private mode for the default user in the Stickfix database. This function checks that the
         * `setPrivateMode` method correctly prevents updates to the default user's settings and returns a failure.
         *
         * @param database The StickfixDatabase instance to be tested.
         * @throws AssertionError If the private mode update does not result in a failure or the failure message is incorrect.
         */
        private suspend fun testSettingPrivateModeDefaultUser(database: StickfixDatabase) {
            val user = StickfixUser(STICKFIX_PUBLIC_USERNAME, STICKFIX_PUBLIC_ID)
            checkAll(Arb.enum<PrivateMode>()) { mode ->
                val result = database.setPrivateMode(user, mode)
                assertDatabaseOperationFailed(result, "Cannot update default user's settings")
            }
        }

        /**
         * Tests setting the private mode for a user that does not exist in the Stickfix database. This function verifies that
         * the `setPrivateMode` method correctly handles attempts to update the settings of a non-existent user and returns a failure.
         *
         * @param database The StickfixDatabase instance to be tested.
         * @throws AssertionError If the private mode update does not result in a failure or the failure message is incorrect.
         */
        private suspend fun testSettingPrivateModeNonExistentUser(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database.database))),
                arbInitDatabaseWithUserNotInDatabase(database.database),
                Arb.enum<PrivateMode>()
            ) { user, mode ->
                val result = database.setPrivateMode(user, mode)
                assertDatabaseOperationFailed(result, "User must exist in the database")
            }
        }

        /**
         * Tests setting the private mode for an existing user in the Stickfix database. This function verifies that the
         * `setPrivateMode` method successfully updates the private mode setting for a user that exists in the database.
         *
         * @param database The StickfixDatabase instance to be tested.
         * @throws AssertionError If the private mode update does not result in a success or the updated mode is incorrect.
         */
        private suspend fun testSettingPrivateModeExistentUser(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database.database))),
                arbInitDatabaseWithUserInDatabase(database.database),
                Arb.enum<PrivateMode>()
            ) { user, mode ->
                database.setPrivateMode(user, mode)
                    .shouldBeRight()
                    .shouldNotBeNull()
                    .data.toPrivateMode() shouldBe mode
            }
        }

        /**
         * Tests setting the shuffle mode for the default user in the Stickfix database. This function checks that the
         * `setShuffle` method correctly prevents updates to the default user's settings and returns a failure.
         *
         * @param database The StickfixDatabase instance to be tested.
         */
        private suspend fun testSettingShuffleModeDefaultUser(database: StickfixDatabase) {
            val user = StickfixUser(STICKFIX_PUBLIC_USERNAME, STICKFIX_PUBLIC_ID)
            checkAll(Arb.enum<ShuffleMode>()) { mode ->
                val result = database.setShuffle(user, mode)
                assertDatabaseOperationFailed(result, "Cannot update default user's settings")
            }
        }

        /**
         * Tests setting the shuffle mode for a user that does not exist in the Stickfix database. This function
         * verifies that the `setShuffle` method correctly handles attempts to update the settings of a non-existent
         * user and returns a failure.
         *
         * @param database The StickfixDatabase instance to be tested.
         */
        private suspend fun testSettingShuffleModeNonExistentUser(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database.database))),
                arbInitDatabaseWithUserNotInDatabase(database.database),
                Arb.enum<ShuffleMode>()
            ) { user, mode ->
                val result = database.setShuffle(user, mode)
                assertDatabaseOperationFailed(result, "User must exist in the database")
            }
        }

        /**
         * Tests setting the shuffle mode for an existing user in the Stickfix database. This function verifies that the
         * `setShuffle` method successfully updates the shuffle mode setting for a user that exists in the database.
         *
         * @param database The StickfixDatabase instance to be tested.
         */
        private suspend fun testSettingShuffleModeExistentUser(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetUsersTableListener(database.database))),
                arbInitDatabaseWithUserInDatabase(database.database),
                Arb.enum<ShuffleMode>()
            ) { user, mode ->
                database.setShuffle(user, mode)
                    .shouldBeRight()
                    .shouldNotBeNull()
                    .data.toShuffleMode() shouldBe mode
            }
        }
    }
}

/**
 * Converts a `Boolean` value to its corresponding `ShuffleMode` enum value.
 *
 * This extension function interprets the `Boolean` as follows:
 * - `true` is converted to `ShuffleMode.ENABLED`.
 * - `false` is converted to `ShuffleMode.DISABLED`.
 *
 * @receiver Boolean The boolean value to be converted.
 * @return ShuffleMode The corresponding `ShuffleMode` enum value based on the boolean value.
 */
private fun Boolean.toShuffleMode() = if (this) ShuffleMode.ENABLED else ShuffleMode.DISABLED

/**
 * Converts a `Boolean` value to its corresponding `PrivateMode` enum value.
 *
 * This extension function interprets the `Boolean` as follows:
 * - `true` is converted to `PrivateMode.ENABLED`.
 * - `false` is converted to `PrivateMode.DISABLED`.
 *
 * @receiver Boolean The boolean value to be converted.
 * @return PrivateMode The corresponding `PrivateMode` enum value based on the boolean value.
 */
private fun Boolean.toPrivateMode() = if (this) PrivateMode.ENABLED else PrivateMode.DISABLED

/**
 * Generates an arbitrary string of hexadecimal characters to be used as a key. The generated string has a length
 * between 1 and 16 characters. This function leverages the Kotest's property-based testing framework to create random
 * hexadecimal strings that can be used in various tests where a random key is required.
 *
 * @return An `Arb<String>` instance that generates random hexadecimal strings with lengths between 1 and 16.
 */
private fun arbKey(): Arb<String> = Arb.string(1..16, Codepoint.hex())

/**
 * Generates an arbitrary `StickfixUser` that is not present in the given database. This function relies on an existing
 * arbitrary that initializes the database with users and maps it to produce a `StickfixUser` instance with the
 * specified ID that is not already present in the database.
 *
 * @param database The `Database` instance where the user should not be present.
 * @return An `Arb<StickfixUser>` that generates `StickfixUser` instances with IDs not present in the specified
 *   database.
 */
private fun arbInitDatabaseWithUserNotInDatabase(database: Database): Arb<StickfixUser> =
    arbInitDatabaseWithUserIdNotInDatabase(database).map { StickfixUser("user$it", it) }

/**
 * Generates an arbitrary `StickfixUser` that is already present in the database. This function uses a previously
 * initialized list of user IDs in the database and maps them to corresponding `StickfixUser` instances.
 *
 * @param database The database instance where the users are stored.
 * @return An `Arb<StickfixUser>` representing a user that is already present in the database.
 */
private fun arbInitDatabaseWithUserInDatabase(database: Database): Arb<StickfixUser> =
    arbInitDatabaseWithUserIdInDatabase(database).map { StickfixUser("user$it", it) }


/**
 * A `PropTestListener` implementation that resets the `Meta` table in the `StickfixDatabase` before and after each test.
 *
 * This class is used to ensure a clean state of the `Meta` table for each property-based test. Before each test, the
 * database is initialized, ensuring that the `Meta` table is created and ready for use. After each test, the `Meta`
 * table is dropped, removing any data that was inserted during the test. This prevents any leftover state from
 * affecting subsequent tests, ensuring test isolation and reliability.
 *
 * @property database The `StickfixDatabase` instance that contains the `Meta` table to be reset.
 */
private class ResetMetaTableAfterEach(private val database: StickfixDatabase) : PropTestListener {

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
