/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.chat

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.State
import cl.ravenhill.stickfix.states.TransitionResult
import com.github.kotlintelegrambot.Bot
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Defines a minimal set of user information necessary for reading purposes within applications.
 * This interface is designed to be implemented by classes that handle user identity attributes,
 * providing a standardized approach to accessing essential user information such as username,
 * user ID, and the current state in a state-driven system.
 *
 * ## Usage:
 * Implement this interface in any class where user-specific data needs to be encapsulated
 * alongside user state management. This setup is especially useful in applications following
 * a state machine pattern, allowing user states to directly manage transitions and interactions.
 *
 * ### Example 1: Implementing ReadUser
 * ```kotlin
 * class ConcreteUser(override val username: String,
 *                    override val userId: Long,
 *                    override var state: State) : ReadUser {
 *     // Additional functionalities can be implemented here
 * }
 * ```
 *
 * @property username A string representing the user's username. This is a read-only property and is
 * typically used to display user identity or for logging purposes.
 * @property userId A long representing the unique identifier for the user. This property provides
 * a unique way to reference the user throughout the application, crucial for tracking user activities
 * or database operations.
 * @property state The current `State` of the user within the application. This property is essential
 * for managing state-driven interactions, facilitating the correct response to user commands based
 * on their current state.
 */
interface ReadUser {
    val username: String
    val userId: Long
    val state: State

    /**
     * Initiates the user's current state handling of the start action. This method simplifies the interaction
     * between a user and their state by allowing direct triggering of state-specific behavior from the user object.
     * It provides a transparent way to manage state transitions triggered by user actions.
     * @param bot A `TelegramBot` instance required to interact with the Telegram API, such as sending messages
     * or handling commands, based on the user's current state.
     * @return TransitionResult The result of the state transition attempt, indicating success or failure
     * and the next state to transition into, if applicable.
     */
    fun onStart(bot: TelegramBot): TransitionResult = state.onStart(bot)

    /**
     * Provides a concise string representation of the user, useful for logging and debugging
     * purposes. This property returns the user's username if it is not blank, or the user ID
     * otherwise, ensuring that there is always a meaningful identifier available.
     *
     * ## Usage:
     * This property is used primarily in logging and error messages where user identification is
     * necessary but privacy and clarity must be maintained.
     *
     * ### Example:
     * ```kotlin
     * logger.info("Processing request for user: ${user.debugInfo}")
     * ```
     */
    val debugInfo: String get() = username.ifBlank { userId.toString() }

    /**
     * Manages the transition of this user to an idle state. This method is called when the user's interaction is
     * minimized or deemed inactive, requiring updates to their record in the database to reflect this new state.
     *
     * @param bot The bot instance currently managing user interactions.
     * @return
     *  `TransitionResult` - The outcome of the idle transition, typically indicating the successful update of the
     *  user's state.
     */
    fun onIdle(bot: TelegramBot): TransitionResult {
        transaction {
            Users.update({ Users.id eq userId }) {
                it[state] = IdleState::class.simpleName!!
            }
        }
        return state.onIdle(bot)
    }
}
