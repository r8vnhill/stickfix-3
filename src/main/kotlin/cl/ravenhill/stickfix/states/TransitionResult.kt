/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.states.base.State

/**
 * Represents the result of a state transition within StickFix. This sealed interface defines a common structure for all
 * transition results, ensuring that each result includes the next state of the user, whether the transition was
 * successful or not.
 *
 * @property nextState The `State` instance representing the user's next state after the transition.
 */
sealed interface TransitionResult {
    val nextState: State
}

/**
 * Represents a successful state transition. This data class is used when a transition from one state to another is
 * successfully completed, encapsulating the next state of the user.
 *
 * @property nextState The `State` instance representing the user's next state after the successful transition.
 */
data class TransitionSuccess(override val nextState: State) : TransitionResult

/**
 * Represents a failed state transition. This data class is used when a transition from one state to another fails,
 * encapsulating the state that the user remains in after the failed transition.
 *
 * @property nextState The `State` instance representing the user's state after the failed transition.
 */
data class TransitionFailure(override val nextState: State) : TransitionResult
