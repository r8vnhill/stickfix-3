package cl.ravenhill.stickfix.chat.handlers

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.states.SealedState

/**
 * The `IdleHandler` interface defines a method for transitioning a user to the idle state within StickFix. This
 * interface delegates the handling of the idle transition to the current state of the user, allowing the state-specific
 * logic for transitioning to idle to be executed.
 *
 * @property state The current `State` instance representing the user's state within the application.
 */
interface IdleHandler {
    val state: SealedState

    /**
     * Transitions the user to the idle state by delegating the call to the current state's `onIdle` method. This
     * function ensures that the transition logic defined in the current state is executed, allowing the user to be
     * moved to the idle state according to the rules defined by the current state.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the database service.
     * @return `TransitionResult` indicating the result of the transition to the idle state, as determined by the
     *   current state's `onIdle` method.
     */
    context(StickfixBot)
    fun onIdle() = state.onIdle()
}
