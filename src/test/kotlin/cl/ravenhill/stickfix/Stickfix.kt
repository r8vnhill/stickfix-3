/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix

import cl.ravenhill.stickfix.chat.StickfixUser
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbs.Username
import io.kotest.property.arbs.usernames

fun arbStickfixUser(
    username: Arb<Username> = Arb.usernames(),
    id: Arb<Long> = Arb.long(),
): Arb<StickfixUser> = arbitrary {
    StickfixUser(username.bind().value, id.bind())
}
