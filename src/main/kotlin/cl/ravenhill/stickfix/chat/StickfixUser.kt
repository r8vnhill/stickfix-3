/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.chat.handlers.IdleHandler
import cl.ravenhill.stickfix.chat.handlers.PrivateModeHandler
import cl.ravenhill.stickfix.chat.handlers.RevokationHandler
import cl.ravenhill.stickfix.chat.handlers.ShuffleHandler
import cl.ravenhill.stickfix.chat.handlers.StartHandler
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.SealedState
import com.github.kotlintelegrambot.entities.User as TelegramUser

data class StickfixUser(
    val username: String,
    override val id: Long,
) : StickfixChat, StartHandler, IdleHandler, RevokationHandler, PrivateModeHandler, ShuffleHandler {
    /**
     * The state of the user within the StickFix bot. This property manages the current condition or phase
     * of interaction the user is in, which can dictate the bot's responses and actions.
     */
    override var state: SealedState = IdleState(this)

    /**
     * Provides a concise string representation of the user, useful for logging and debugging purposes. This property
     * returns the username if available, or the user ID otherwise.
     *
     * @return String The debug information for the user.
     */
    override val debugInfo: String get() = if (username.isNotBlank()) "'$username'" else id.toString()

    override fun toString() = "StickfixUser(username='$username', id=$id, state=${state::class.simpleName})"

    companion object {
        /**
         * Creates a `StickfixUser` instance from a given `TelegramUser`. This function extracts the necessary
         * information from the `TelegramUser` and uses it to create a new `StickfixUser`.
         *
         * @param from The `TelegramUser` instance from which to create the `StickfixUser`.
         * @return A new `StickfixUser` instance with the username and ID extracted from the `TelegramUser`.
         */
        fun from(from: TelegramUser) = StickfixUser(from.username ?: "unknown", from.id)
    }
}
