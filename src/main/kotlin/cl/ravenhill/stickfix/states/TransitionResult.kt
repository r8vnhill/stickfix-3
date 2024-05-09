/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

/**
 * Defines a structure for the outcome of state transitions within an application. This interface
 * ensures that all state transition results, whether successful or not, provide information about
 * the next state to which the application will transition. This can be particularly useful in
 * systems that follow a state machine pattern, where actions lead to state changes based on success
 * or failure conditions.
 *
 * ## Usage:
 * Implement this interface to encapsulate the result of a transition operation in state-driven
 * systems. The implementing class must specify the next state, which aids in determining the flow
 * of the application following the operation.
 *
 * ### Example 1: Implementing TransitionResult
 * ```kotlin
 * class SomeStateHandler {
 *     fun processTransition(currentState: State): TransitionResult {
 *         return if (currentState.canProceed()) {
 *             TransitionSuccess(nextState = State.FINISHED)
 *         } else {
 *             TransitionFailure(nextState = State.ERROR)
 *         }
 *     }
 * }
 * ```
 *
 * @property nextState
 *  The state to which the system will transition after the current operation. This could be the
 *  next logical state on success or a specific error state on failure.
 */
interface TransitionResult {
    val nextState: State
}

/**
 * Represents a successful outcome of a state transition, indicating that the operation or action
 * leading to this result was successful and the system can move to the specified `nextState`.
 *
 * @param nextState The state to which the system will transition after a successful operation.
 */
class TransitionSuccess(override val nextState: State) : TransitionResult

/**
 * Represents an unsuccessful outcome of a state transition, indicating that the operation or action
 * leading to this result encountered issues, thus transitioning the system to an error or alternative
 * state as specified by `nextState`.
 *
 * @param nextState The state to which the system will transition after a failed operation, typically
 * an error or recovery state.
 */
class TransitionFailure(override val nextState: State) : TransitionResult
