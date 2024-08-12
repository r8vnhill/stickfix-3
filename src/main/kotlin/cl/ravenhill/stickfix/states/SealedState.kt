/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.states.base.IdleTransition
import cl.ravenhill.stickfix.states.base.IdleTransitionImpl
import cl.ravenhill.stickfix.states.base.PrivateTransition
import cl.ravenhill.stickfix.states.base.PrivateTransitionImpl
import cl.ravenhill.stickfix.states.base.RevokeTransition
import cl.ravenhill.stickfix.states.base.RevokeTransitionImpl
import cl.ravenhill.stickfix.states.base.ShuffleTransition
import cl.ravenhill.stickfix.states.base.ShuffleTransitionImpl
import cl.ravenhill.stickfix.states.base.StartTransition
import cl.ravenhill.stickfix.states.base.StartTransitionImpl

/**
 * Represents a state in a state-driven application, providing a common interface for handling state-specific actions
 * and transitions. As a sealed interface, `State` ensures that all potential states are known at compile time, allowing
 * for exhaustive checking of state types. Each state encapsulates its own behavior and context, facilitating a robust
 * and scalable state management system.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows
 *   the state to have direct access to and modify user data as necessary during state transitions.
 */
sealed class SealedState(user: StickfixUser) :
    StartTransition by StartTransitionImpl(user),
    IdleTransition by IdleTransitionImpl(user),
    RevokeTransition by RevokeTransitionImpl(user),
    PrivateTransition by PrivateTransitionImpl(user),
    ShuffleTransition by ShuffleTransitionImpl(user)
