/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadWriteUser

/**
 * Represents the idle state for a user in the Stickfix bot application. This state indicates that the user is currently
 * not engaged in any specific action. The `IdleState` class implements the `State` interface, allowing for state
 * transitions such as starting a new action or revoking the bot.
 *
 * @property context A `ReadWriteUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
class IdleState(override val context: ReadWriteUser) : State {
    init {
        context.state = this
    }

    /**
     * Handles the transition from idle state to start state when the user initiates a start action. Updates the user's
     * state to `StartState` and returns a successful transition result.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition to `StartState`, indicating success.
     */
    override fun onStart(bot: StickfixBot): TransitionResult {
        context.state = StartState(context)
        return TransitionSuccess(context.state)
    }

    /**
     * Handles the transition from idle state to revoke state when the user initiates a revocation action. Updates the
     * user's state to `RevokeState` and returns a successful transition result.
     *
     * @param bot The `StickfixBot` instance representing the bot that processes the interaction.
     * @return TransitionResult The result of the transition to `RevokeState`, indicating success.
     */
    override fun onRevoke(bot: StickfixBot): TransitionResult {
        context.state = RevokeState(context)
        return TransitionSuccess(context.state)
    }
}
