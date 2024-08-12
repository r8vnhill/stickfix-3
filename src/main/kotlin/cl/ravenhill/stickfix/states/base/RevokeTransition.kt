package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionResult

/**
 * The `RevokeTransition` interface defines methods for handling the revocation process within StickFix.
 * Implementing classes are expected to provide the logic for managing user revocation actions, confirming
 * revocation, and handling the rejection of a revocation.
 */
interface RevokeTransition {

    /**
     * Handles the revocation process for the user in the current state. This method is responsible for managing the
     * logic associated with revoking a user's registration or access, as defined by the implementing class.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage state transitions.
     * @return TransitionResult The result of the revocation process, indicating success or failure.
     */
    context(StickfixBot)
    fun onRevoke(): TransitionResult

    /**
     * Handles the confirmation of a revocation action within the current state. This method manages the logic for
     * confirming that a revocation should proceed, typically transitioning the user to a different state or finalizing
     * the revocation.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage state transitions.
     * @return TransitionResult The result of the confirmation process, indicating success or failure.
     */
    context(StickfixBot)
    fun onRevokeConfirmation(): TransitionResult

    /**
     * Handles the rejection of a revocation action within the current state. This method manages the logic for
     * canceling or rejecting a revocation, typically keeping the user's current state unchanged or transitioning
     * to another appropriate state.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage state transitions.
     * @return TransitionResult The result of the rejection process, indicating success or failure.
     */
    context(StickfixBot)
    fun onRevokeRejection(): TransitionResult
}

/**
 * The `RevokeTransitionImpl` class provides a concrete implementation of the `RevokeTransition` interface.
 * It extends `AbstractState`, allowing it to utilize shared state-related functionality while implementing the
 * logic required to handle revocation actions, revocation confirmations, and revocation rejections.
 *
 * @property user The `StickfixUser` instance representing the user associated with this state.
 */
class RevokeTransitionImpl(user: StickfixUser) : AbstractState(user), RevokeTransition {

    private val action = "revoke"

    /**
     * Handles the revocation process in the current state. This method logs a transition failure when the user
     * attempts to revoke from the current state and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage state transitions.
     * @return TransitionResult Indicates the failure to transition from the current state during revocation.
     */
    context(StickfixBot)
    override fun onRevoke(): TransitionResult {
        logTransitionFailure(action)
        return TransitionFailure(this)
    }

    /**
     * Handles the confirmation of the revoke action within the current state. This method logs a transition failure
     * when the user attempts to confirm the revoke action and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult Indicates the failure to transition to a state representing the revoke action.
     */
    context(StickfixBot)
    override fun onRevokeConfirmation(): TransitionResult {
        logTransitionFailure(action)
        return TransitionFailure(this)
    }

    /**
     * Handles the rejection of the revoke action within the current state. This method logs a transition failure
     * when the user attempts to reject the revoke action and returns a `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult Indicates the failure to transition from the current state during the revoke rejection.
     */
    context(StickfixBot)
    override fun onRevokeRejection(): TransitionResult {
        logTransitionFailure(action)
        return TransitionFailure(this)
    }
}
