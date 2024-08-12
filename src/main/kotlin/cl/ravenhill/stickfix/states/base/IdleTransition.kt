package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.TransitionResult
import cl.ravenhill.stickfix.states.TransitionSuccess

/**
 * The `IdleTransition` interface defines a method for transitioning a user to the idle state within StickFix.
 * Implementing classes are expected to provide the logic for this transition, allowing the user's state to be set to
 * idle when appropriate.
 */
interface IdleTransition {

    /**
     * Transitions the user to the idle state. This method is responsible for managing the logic that transitions the
     * user to an idle state, typically by updating the user's state and performing any necessary actions associated
     * with this transition.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the database service.
     * @return TransitionResult The result of the transition to the idle state, indicating whether the transition was
     *   successful or not.
     */
    context(StickfixBot)
    fun onIdle(): TransitionResult
}

/**
 * The `IdleTransitionImpl` class provides a concrete implementation of the `IdleTransition` interface. It extends
 * `AbstractState`, allowing it to utilize shared state-related functionality while implementing the logic required
 * to transition a user to the idle state.
 *
 * @property user The `StickfixUser` instance representing the user associated with this state.
 */
open class IdleTransitionImpl(user: StickfixUser) : AbstractState(user), IdleTransition {

    /**
     * Transitions the user to the idle state. This function sets the user's state to `IdleState`, updates the state in
     * the database, and returns a `TransitionSuccess` indicating that the transition to the idle state was successful.
     *
     * @receiver StickfixBot The bot instance used to interact with the bot's functionalities and manage the database
     *   service.
     * @return TransitionResult indicating the success of the transition to the idle state.
     */
    context(StickfixBot)
    override fun onIdle(): TransitionResult {
        databaseService.setUserState(user, ::IdleState)
        return TransitionSuccess(user.state)
    }
}
