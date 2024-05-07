/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package commands

import chat.ReadUser

data class StartCommand(
    override val user: ReadUser
) : Command
