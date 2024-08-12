package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `StartBase` interface defines the structure for handling start-related actions within a user interaction in
 * StickFix. It provides methods to handle the initiation, rejection, and confirmation of a start action, allowing
 * classes that implement this interface to manage these actions according to the user's current state.
 */
interface StartTransition : State {

    /**
     * Handles the start of an interaction within the current state. This method is expected to manage the logic
     * associated with initiating a start action, potentially transitioning the user to a different state.
     *
     * @return TransitionResult The result of the start action, indicating whether the transition to a new state was
     *   successful or not.
     */
    context(StickfixBot)
    fun onStart(): TransitionResult

    /**
     * Handles the rejection of a start action within the current state. This method should manage the logic associated
     * with a user rejecting the start action, potentially transitioning the user to a different state that reflects the
     * rejection.
     *
     * @return TransitionResult The result of the rejection handling, indicating whether the transition to a new state
     *   was successful or not.
     */
    context(StickfixBot)
    fun onStartRejection(): TransitionResult

    /**
     * Handles the confirmation of a start action within the current state. This method should manage the logic
     * associated with a user confirming the start action, potentially transitioning the user to a different state that
     * reflects the confirmation.
     *
     * @return TransitionResult The result of the confirmation handling, indicating whether the transition to a new
     *   state was successful or not.
     */
    context(StickfixBot)
    fun onStartConfirmation(): TransitionResult
}

/**
 * The `StartBaseImpl` class provides a default implementation of the `StartBase` interface. It extends `AbstractState`,
 * allowing it to leverage shared state-related functionality while implementing the start-related actions defined in
 * `StartBase`. This class is used to manage the start, rejection, and confirmation actions within a specific user
 * state.
 *
 * @property user The `StickfixUser` instance representing the user associated with this state.
 */
class StartTransitionImpl(user: StickfixUser) : AbstractState(user), StartTransition {

    /**
     * Handles the start of an interaction within this state. By default, this method logs a warning
     * indicating an unauthorized or unexpected attempt to start from the current state and returns
     * a `TransitionFailure` with the current state as the next state, suggesting no transition
     * occurs.
     *
     * @return TransitionResult Indicates the failure to transition from this state, typically because the action is not
     *   allowed or valid in the current context.
     */
    context(StickfixBot)
    override fun onStart(): TransitionResult {
        logTransitionFailure("start")
        return TransitionFailure(this)
    }

    /**
     * Handles the rejection of the start command within the current state by logging an error message and returning a
     * `TransitionFailure`. This function is called when a user attempts to reject the start command, indicating that
     * the transition to a state representing the rejection of the start command has failed.
     *
     * @return `TransitionResult` indicating the failure to transition to a state representing the rejection of the
     *   start command.
     */
    context(StickfixBot)
    override fun onStartRejection(): TransitionResult {
        logTransitionFailure("reject start")
        return TransitionFailure(this)
    }

    /**
     * Handles the confirmation of the start action for the current state of the user. This function logs an error
     * message indicating that the user attempted to confirm the start action from an invalid state and returns a
     * `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult The result of the start confirmation transition, indicating failure due to an invalid
     *   state.
     */
    context(StickfixBot)
    override fun onStartConfirmation(): TransitionResult {
        logTransitionFailure("confirm start")
        return TransitionFailure(this)
    }
}
