package cl.ravenhill.stickfix.chat.handlers

import cl.ravenhill.stickfix.Stateful
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `ShuffleHandler` interface defines methods for handling shuffle-related actions within StickFix. It extends the
 * `Stateful` interface, ensuring that any implementing class is associated with a `State`. The interface provides
 * functions to initiate shuffling, enable shuffle mode, and disable shuffle mode, with the actual logic delegated to
 * the user's current state.
 */
interface ShuffleHandler : Stateful {

    /**
     * Handles the shuffle action for the current state of the user in StickFix. This method delegates the shuffle
     * action to the current state of the user by invoking the `onShuffle` method of that state.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage state transitions.
     * @return `TransitionResult` The result of the shuffle action, which can either be a success, indicating a
     *         successful transition to shuffle mode, or a failure if the transition was not possible.
     */
    context(StickfixBot)
    fun onShuffle(): TransitionResult = state.onShuffle()

    /**
     * Initiates the process to enable shuffle mode for the current state of the user. This function delegates the
     * operation to the state's `onShuffleEnabled` method, allowing the state to handle the transition to shuffle mode
     * according to its specific logic.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage state transitions.
     * @return `TransitionResult` The result of the shuffle mode enabling process, indicating success or failure.
     */
    context(StickfixBot)
    fun onShuffleEnabled(): TransitionResult = state.onShuffleEnabled()

    /**
     * Initiates the process to disable shuffle mode for the current state of the user. This function delegates the
     * operation to the state's `onShuffleDisabled` method, allowing the state to handle the transition to disable
     * shuffle mode according to its specific logic.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage state transitions.
     * @return `TransitionResult` The result of the shuffle mode disabling process, indicating success or failure.
     */
    context(StickfixBot)
    fun onShuffleDisabled(): TransitionResult = state.onShuffleDisabled()
}
