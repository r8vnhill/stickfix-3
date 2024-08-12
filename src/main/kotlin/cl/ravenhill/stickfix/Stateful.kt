package cl.ravenhill.stickfix

import cl.ravenhill.stickfix.states.SealedState

/**
 * The `Stateful` interface represents an entity that has an associated state within StickFix. Any class implementing
 * this interface must define a `State` property that represents its current state.
 *
 * @property state The current `State` instance representing the state of the implementing entity within StickFix.
 */
interface Stateful {
    val state: SealedState
}
