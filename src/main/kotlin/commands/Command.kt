/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package commands

import chat.ReadUser

sealed interface Command {
    val user: ReadUser
}