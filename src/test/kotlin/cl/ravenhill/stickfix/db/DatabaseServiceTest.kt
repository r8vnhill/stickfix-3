/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.db

import cl.ravenhill.stickfix.db.DatabaseService
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class DatabaseServiceTest : FreeSpec(
    {
        "A DatabaseService" - {
            "has an API token key" {
                DatabaseService.API_TOKEN_KEY shouldBe "API_KEY"
            }
        }
    }
)