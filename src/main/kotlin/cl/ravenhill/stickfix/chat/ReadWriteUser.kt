/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.states.State

/**
 * Extends the `ReadUser` interface to include write capabilities, allowing for modifications
 * to a user's state. This interface is designed for scenarios where user interactions require
 * changing their state within the system, such as during conversations or transactional operations
 * with a Telegram bot.
 *
 * ## Usage:
 * Implement this interface in classes that manage user data where the user's state needs to be
 * dynamically updated based on interactions within the application. This is particularly useful
 * in systems that follow a state machine pattern to handle various stages of user interaction or
 * processing.
 *
 * ### Example 1: Implementing ReadWriteUser
 * ```kotlin
 * class ConcreteUser(override var username: String, override var userId: Long) : ReadWriteUser {
 *     override var state: State = InitialState
 *
 *     fun updateState(newState: State) {
 *         state = newState
 *     }
 * }
 * ```
 *
 * @property state
 *  A mutable property representing the user's current state. This state is used to track and manage
 *  the user's progression or status within the application. Implementations must provide mechanisms
 *  to get and set this state, reflecting changes that occur during application execution.
 */
interface ReadWriteUser : ReadUser {
    override var state: State
}
