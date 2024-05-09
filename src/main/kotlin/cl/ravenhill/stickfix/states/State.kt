/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadWriteUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Represents a state in a state-driven application, providing a common interface for handling
 * state-specific actions and transitions. As a sealed interface, `State` ensures that all potential
 * states are known at compile time, allowing for exhaustive checking of state types. Each state
 * encapsulates its own behavior and context, facilitating a robust and scalable state management
 * system.
 *
 * ## Usage:
 * Implement this interface for each distinct state in your application's state machine. Each state
 * should define how it handles the start of an interaction, typically involving a transition to
 * another state or logging an attempt to perform an invalid action.
 *
 * ### Example 1: Implementing State with onStart Method
 * ```kotlin
 * object InitialState : State {
 *     override val context: ReadWriteUser = // initialize context
 *
 *     override fun onStart(bot: TelegramBot): TransitionResult {
 *         // Implementation for when the state is started
 *         return TransitionSuccess(NextState)
 *     }
 * }
 * ```
 *
 * @property context
 *  A `ReadWriteUser` instance representing the user information relevant to the state. This allows
 *  the state to have direct access to and modify user data as necessary during state transitions.
 */
sealed interface State {
    val context: ReadWriteUser

    /**
     * A logger instance for logging state-related actions. This logger is private to the state and
     * is used to record activities such as transitions and errors.
     */
    private val logger: Logger get() = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the start of an interaction within this state. By default, this method logs a warning
     * indicating an unauthorized or unexpected attempt to start from the current state and returns
     * a `TransitionFailure` with the current state as the next state, suggesting no transition
     * occurs.
     *
     * @param bot
     *  A `TelegramBot` instance, allowing the state to interact with the Telegram bot, such as
     *  sending messages or commands.
     * @return TransitionResult
     *  Indicates the failure to transition from this state, typically because the action is not
     *  allowed or valid in the current context.
     */
    fun onStart(bot: TelegramBot): TransitionResult {
        logger.warn(
            "User ${context.username.ifBlank { context.userId.toString() }} tried to start from state ${javaClass.simpleName}"
        )
        return TransitionFailure(this)
    }
}
