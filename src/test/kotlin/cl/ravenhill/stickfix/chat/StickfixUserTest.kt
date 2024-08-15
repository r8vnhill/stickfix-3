package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.STICKFIX_DEFAULT_USER_ID
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbs.Username
import io.kotest.property.arbs.usernames

class StickfixUserTest : FreeSpec({


})

/**
 * Generates an arbitrary `StickfixUser` instance with a specified username and ID.
 *
 * This function creates a `StickfixUser` by first filtering out any IDs that match the default user ID
 * (`STICKFIX_DEFAULT_USER_ID`), ensuring that the generated users are not the default user. It then pairs a valid,
 * non-default user ID with a generated username to create a `StickfixUser`.
 *
 * @param username An `Arb<Username>` that generates arbitrary usernames for the user.
 * @param id An `Arb<Long>` that generates arbitrary user IDs, which are filtered to exclude the default user ID.
 * @return An `Arb<StickfixUser>` that produces arbitrary `StickfixUser` instances with valid usernames and IDs.
 */
internal fun arbUser(
    username: Arb<Username> = Arb.usernames(),
    id: Arb<Long> = Arb.long()
): Arb<StickfixUser> =
    id.filter { it != STICKFIX_DEFAULT_USER_ID }
        .flatMap { validId ->
            username.map { name ->
                StickfixUser(name.value, validId)
            }
        }
