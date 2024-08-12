package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `ShuffleTransition` interface defines the contract for handling shuffle-related transitions within StickFix.
 * Implementing classes are expected to manage the logic for shuffling stickers, as well as enabling and disabling
 * shuffle mode for a user.
 */
interface ShuffleTransition {

    /**
     * Handles the shuffle action for the current state of the user. Implementing classes should define the logic for
     * shuffling stickers within the user's current context.
     *
     * @return `TransitionResult` The result of the shuffle operation, which may indicate success or failure depending
     *   on the state.
     */
    fun onShuffle(): TransitionResult

    /**
     * Enables shuffle mode for the user. Implementing classes should define the logic for transitioning the user to
     * shuffle mode, allowing their stickers to be shuffled with each request.
     *
     * @return `TransitionResult` The result of enabling shuffle mode, which may indicate success or failure depending
     *   on the state.
     */
    fun onShuffleEnabled(): TransitionResult

    /**
     * Disables shuffle mode for the user. Implementing classes should define the logic for transitioning the user out
     * of shuffle mode, so their stickers are no longer shuffled.
     *
     * @return `TransitionResult` The result of disabling shuffle mode, which may indicate success or failure depending
     *   on the state.
     */
    fun onShuffleDisabled(): TransitionResult
}

/**
 * The `ShuffleTransitionImpl` class provides a concrete implementation of the `ShuffleTransition` interface.
 * It extends `AbstractState`, allowing it to utilize shared state-related functionality while implementing the logic
 * required to handle shuffle transitions, including enabling and disabling shuffle mode.
 *
 * @property user The `StickfixUser` instance representing the user associated with this state.
 */
class ShuffleTransitionImpl(user: StickfixUser) : AbstractState(user), ShuffleTransition {

    /**
     * Handles the shuffle action in the current state. This function logs a transition failure since shuffling is not
     * supported in the current state. It then returns a `TransitionFailure` result, indicating that the shuffle
     * operation was unsuccessful.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return `TransitionResult` representing the failure to transition to a state that supports shuffling. The result
     *   is a `TransitionFailure` containing the current state.
     */
    context(StickfixBot)
    override fun onShuffle(): TransitionResult {
        logTransitionFailure("shuffle")
        return TransitionFailure(this)
    }

    /**
     * Handles the attempt to enable shuffle mode in the current state. This function logs a failure message and returns
     * a `TransitionFailure` indicating that enabling shuffle mode is not allowed or supported in the current state.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return `TransitionResult` The result of the attempt to enable shuffle mode, always indicating failure in this
     *   context.
     */
    context(StickfixBot)
    override fun onShuffleEnabled(): TransitionResult {
        logTransitionFailure("enable shuffle mode")
        return TransitionFailure(this)
    }

    /**
     * Handles the attempt to disable shuffle mode in the current state. This function logs a failure message and
     * returns a `TransitionFailure` indicating that disabling shuffle mode is not allowed or supported in the current
     * state.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return `TransitionResult` The result of the attempt to disable shuffle mode, always indicating failure in this
     *   context.
     */
    context(StickfixBot)
    override fun onShuffleDisabled(): TransitionResult {
        logTransitionFailure("disable shuffle mode")
        return TransitionFailure(this)
    }
}
