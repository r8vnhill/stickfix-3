package cl.ravenhill.stickfix.chat.handlers

import cl.ravenhill.stickfix.Stateful
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `RevocationHandler` interface defines methods for handling the revocation process within StickFix.
 * It extends the `Stateful` interface, ensuring that any implementing class has an associated `State`.
 *
 * This interface provides functions to manage user revocation actions and the confirmation of such actions,
 * delegating the actual logic to the current state of the user.
 */
interface RevocationHandler : Stateful {

    /**
     * Handles the revocation process for the user by invoking the `onRevoke` method on the current state.
     * This method typically updates the user's state in the database and returns the result of the revocation.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's state.
     * @return `TransitionResult` The result of the revocation process, indicating whether it was successful or not.
     */
    context(StickfixBot)
    fun onRevoke() = state.onRevoke()

    /**
     * Facilitates the confirmation of a user's revocation action by delegating the process to the current state.
     * This method calls the `onRevokeConfirmation` function of the user's state to handle the confirmation logic.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's state.
     * @return `TransitionResult` The result of the state transition, indicating the outcome of the revoke confirmation
     *   process.
     */
    context(StickfixBot)
    fun onRevokeConfirmation(): TransitionResult = state.onRevokeConfirmation()

    /**
     * Facilitates the rejection of a user's revocation action by delegating the process to the current state.
     * This method calls the `onRevokeRejection` function of the user's state to handle the rejection logic.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the user's state.
     * @return `TransitionResult` The result of the state transition, indicating the outcome of the revoke rejection
     *   process.
     */
    context(StickfixBot)
    fun onRevokeRejection(): TransitionResult = state.onRevokeRejection()
}
