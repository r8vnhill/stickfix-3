/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotFailure
import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.BotSuccess
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.schema.Users
import cl.ravenhill.stickfix.logDebug
import cl.ravenhill.stickfix.logError
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("states")

/**
 * Represents a state in a state-driven application, providing a common interface for handling state-specific actions
 * and transitions. As a sealed interface, `State` ensures that all potential states are known at compile time, allowing
 * for exhaustive checking of state types. Each state encapsulates its own behavior and context, facilitating a robust
 * and scalable state management system.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows
 *   the state to have direct access to and modify user data as necessary during state transitions.
 */
sealed interface State {
    val user: StickfixUser

    /**
     * A logger instance for logging state-related actions. This logger is private to the state and is used to record
     * activities such as transitions and errors.
     */
    private val logger: Logger get() = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the start of an interaction within this state. By default, this method logs a warning
     * indicating an unauthorized or unexpected attempt to start from the current state and returns
     * a `TransitionFailure` with the current state as the next state, suggesting no transition
     * occurs.
     *
     * @return TransitionResult Indicates the failure to transition from this state, typically because the action
     *                          is not allowed or valid in the current context.
     */
    context(StickfixBot)
    fun onStart(): TransitionResult {
        logError(logger) { "User ${user.debugInfo} attempted to start from state ${javaClass.simpleName}" }
        return TransitionFailure(this)
    }

    /**
     * Processes the user's input text and takes appropriate actions based on the state logic.
     *
     * @param text The input text provided by the user.
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return BotResult<*> The result of processing the input, indicating success or failure.
     */
    fun process(text: String?, bot: StickfixBot): BotResult<*> {
        logDebug(logger) { "Processing input in state ${javaClass.simpleName}" }
        return BotSuccess("Processed input in state ${javaClass.simpleName}", text)
    }

    /**
     * Transitions the user to the idle state. This function sets the user's state to `IdleState`, updates the state in
     * the database, and returns a `TransitionSuccess` indicating that the transition to the idle state was successful.
     *
     * @receiver The `StickfixBot` instance used to interact with the bot's functionalities and manage the database
     *   service.
     * @return `TransitionResult` indicating the success of the transition to the idle state.
     */
    context(StickfixBot)
    fun onIdle(): TransitionResult {
        databaseService.setUserState(IdleState(user))
        return TransitionSuccess(user.state)
    }

    /**
     * Handles the revocation process in the current state.
     *
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return TransitionResult Indicates the failure to transition from the current state during revocation.
     */
    fun onRevoke(bot: StickfixBot): TransitionResult {
        logError(logger) { "User ${user.debugInfo} attempted to revoke from state ${javaClass.simpleName}" }
        return TransitionFailure(this)
    }

    /**
     * Handles the rejection of the start command within the current state by logging an error message and returning a
     * `TransitionFailure`. This function is called when a user attempts to reject the start command, indicating that
     * the transition to a state representing the rejection of the start command has failed.
     *
     * @return `TransitionResult` indicating the failure to transition to a state representing the rejection of the
     *   start command.
     */
    context(StickfixBot)
    fun onStartRejection(): TransitionResult {
        logError(logger) { "User ${user.debugInfo} attempted to reject start from state ${javaClass.simpleName}" }
        return TransitionFailure(this@State)
    }

    /**
     * Handles the confirmation of the start action for the current state of the user. This function logs an error
     * message indicating that the user attempted to confirm the start action from an invalid state and returns a
     * `TransitionFailure`.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return TransitionResult The result of the start confirmation transition, indicating failure due to an invalid
     *   state.
     */
    context(StickfixBot)
    fun onStartConfirmation(): TransitionResult {
        logError(logger) { "User ${user.debugInfo} attempted to confirm start from state ${javaClass.simpleName}" }
        return TransitionFailure(this@State)
    }
}

/**
 * Handles invalid input from the user, sending a clarifying message and logging the event.
 *
 * @param bot The `StickfixBot` instance used to send messages to the user.
 * @param user The `StickfixUser` instance representing the user who provided the invalid input.
 * @param message The message to be sent to the user, clarifying the expected input.
 * @return BotResult<*> The result of sending the message, indicating success or failure.
 */
fun handleInvalidInput(bot: StickfixBot, user: StickfixUser, message: String): BotResult<*> {
    logger.warn("Invalid input from user ${user.debugInfo}")
    return bot.sendMessage(user, message).fold(
        { BotSuccess("Invalid input message sent", it) },
        { BotFailure("Failed to send invalid input message", it) }
    )
}

/**
 * Verifies that the user's state in the database matches the expected state.
 *
 * @param result The result of the previous operation, typically a success.
 * @param expectedState The expected state name that should be matched.
 * @param user The `StickfixUser` instance representing the user whose state is being verified.
 * @return BotResult<*> The result of the verification, indicating success or failure.
 */
fun verifyUserState(result: BotResult<*>, expectedState: String, user: StickfixUser): BotResult<*> {
    if (result is BotSuccess) {
        val isCorrectState = Users.selectAll().where { Users.id eq user.userId }
            .single()[Users.state] == expectedState
        if (!isCorrectState) return BotFailure("User state was not updated", false)
    }
    return result
}

/**
 * Verifies that the user has been deleted from the database.
 *
 * @param result The result of the previous operation, typically a success.
 * @param user The `StickfixUser` instance representing the user whose deletion is being verified.
 * @return BotResult<*> The result of the verification, indicating success or failure.
 */
fun verifyUserDeletion(result: BotResult<*>, user: StickfixUser): BotResult<*> {
    if (result is BotSuccess) {
        val exists = Users.selectAll().where { Users.id eq user.userId }.count() > 0
        if (exists) return BotFailure("User was not deleted", false)
    }
    return result
}
