/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package db

import cl.ravenhill.stickfix.db.StickfixDatabase
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.domain
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class StickfixDatabaseTest : FreeSpec(
    {
        "A StickfixDatabase" - {
            "can be initialized" {
                checkAll(Arb.domain(), Arb.string()) { jdbcUrl, name ->
                    val actual = StickfixDatabase(jdbcUrl, name)
                    val expected = StickfixDatabase(jdbcUrl, name)
                    actual shouldBe expected
                }
            }
        }
    }
)