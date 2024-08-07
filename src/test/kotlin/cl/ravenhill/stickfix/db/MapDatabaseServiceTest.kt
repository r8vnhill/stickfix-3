/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.property.checkAll

class MapDatabaseServiceTest : FreeSpec(
    {
        lateinit var databaseService: MapDatabaseService

        beforeTest {
            databaseService = MapDatabaseService().init()
        }


        "A MapDatabaseService" - {
            "should be initialized" - {
                "with an API_KEY value of empty string" {
                    databaseService.meta["API_KEY"].shouldBeEmpty()
                }
            }

            "can set the value of the API key" {
                checkAll<String> { key ->
                    databaseService.apiToken = key
                    databaseService.apiToken shouldBe key
                }
            }
        }
    }
)
