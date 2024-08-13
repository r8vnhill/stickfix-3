package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.db.schema.Meta
import cl.ravenhill.stickfix.matchers.shouldBeLeft
import cl.ravenhill.stickfix.matchers.shouldBeRight
import cl.ravenhill.stickfix.states.IdleState
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.PropTestListener
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.hex
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
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

        private suspend fun testPresentApiKey(database: StickfixDatabase) {
            checkAll(
                PropTestConfig(listeners = listOf(ResetMetaTableAfterEach(database))),
                arbKey()
            ) { apiKey ->
                transaction(database.database) {
                    Meta.insert {
                        it[key] = "API_KEY"
                        it[value] = apiKey
                    }
                }

                database.queryApiKey()
                    .shouldBeRight()
                    .shouldNotBeNull()
                    .data shouldBe apiKey
            }
        }
    }
}

private const val JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
private const val DRIVER_NAME = "org.h2.Driver"
private const val STICKFIX_PUBLIC_ID = 0L
private const val STICKFIX_PUBLIC_USERNAME = "STICKFIX_PUBLIC"
private val stickfixDefaultUserState = ::IdleState

/**
 * Generates an arbitrary string of hexadecimal characters to be used as a key. The generated string has a length
 * between 1 and 16 characters. This function leverages the Kotest's property-based testing framework to create random
 * hexadecimal strings that can be used in various tests where a random key is required.
 *
 * @return An `Arb<String>` instance that generates random hexadecimal strings with lengths between 1 and 16.
 */
private fun arbKey(): Arb<String> = Arb.string(1..16, Codepoint.hex())

private class ResetMetaTableAfterEach(private val database: StickfixDatabase) : PropTestListener {
    override suspend fun beforeTest() {
        database.init()
    }

    override suspend fun afterTest() {
        transaction(database.database) {
            SchemaUtils.drop(Meta)
        }
    }
}
