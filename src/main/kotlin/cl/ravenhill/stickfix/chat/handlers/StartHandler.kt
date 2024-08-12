package cl.ravenhill.stickfix.chat.handlers

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.states.State
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `StartHandler` interface defines a set of methods for handling start-related actions within a user interaction in
 * the Stickfix bot application. This interface delegates the handling of these actions to the current state of the
 * user, allowing for state-specific logic to be executed based on the user's context.
 *
 * @property state The current `State` instance representing the user's state within the application.
 */
interface StartHandler {
    val state: State

    /**
     * Handles the start of an interaction for the user, delegating the action to the current state. This method ensures
     * that the start logic defined in the current state is executed, which may involve transitioning the user to a
     * different state or handling the start action within the current state.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult The result of the transition attempt, indicating success or failure.
     */
    context(StickfixBot)
    fun onStart(): TransitionResult = state.onStart()

    /**
     * Handles the rejection of an action within the current state by delegating the call to the state's `onRejection`
     * method. This function ensures that the rejection logic defined in the current state is executed, which may
     * involve logging the rejection, transitioning to a different state, or other rejection-specific logic.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return `TransitionResult` indicating the result of the rejection handling, as determined by the current state's
     *   `onRejection` method.
     */
    context(StickfixBot)
    fun onStartRejection(): TransitionResult = state.onStartRejection()

    /**
     * Handles the confirmation of the start action for the current state of the user. This function delegates the
     * handling of the start confirmation to the current state of the user, ensuring that the appropriate state-specific
     * logic is executed.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult The result of the start confirmation transition, indicating success or failure.
     */
    context(StickfixBot)
    fun onStartConfirmation(): TransitionResult = state.onStartConfirmation()
}
