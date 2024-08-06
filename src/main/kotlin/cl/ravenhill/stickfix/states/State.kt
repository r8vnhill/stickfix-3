/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.BotFailure
import cl.ravenhill.stickfix.bot.BotResult
import cl.ravenhill.stickfix.bot.BotSuccess
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.chat.ReadWriteUser
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("states")

/**
 * Defines the behavior and properties of a state in a state-driven application, such as those used in complex bots or
 * applications with stateful interactions. This sealed interface ensures that all concrete state implementations are
 * known at compile-time, facilitating exhaustive checking and robust handling of state transitions.
 *
 * ## Usage:
 * Implement this interface to define specific behaviors for different states within your application's state machine.
 * Each implementation should handle starting interactions, processing messages, and managing idle conditions in ways
 * that are appropriate for the state's role in the application.
 *
 * ### Example 1: Implementing a State
 * ```kotlin
 * object InitialState : State {
 *     override val context: ReadWriteUser = // Initialize with user context
 *
 *     override fun onStart(bot: TelegramBot): TransitionResult {
 *         // Logic to handle the start of an interaction
 *         return TransitionSuccess(NextState)
 *     }
 * }
 * ```
 *
 * @property context A `ReadWriteUser` instance that provides contextual user information necessary for the state's
 *   operations, allowing states to access and modify user data during transitions.
 */
sealed interface State {
    val context: ReadWriteUser

    /**
     * A private logger configured to log state-related activities. It records transitions, actions, and any exceptions
     * or irregular activities occurring within the state.
     */
    private val logger: Logger get() = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the initiation of an interaction within this state. Typically, logs and returns a failure unless
     * overridden by a specific state implementation that supports starting interactions.
     *
     * @param bot A `TelegramBot` instance, providing mechanisms for the state to interact with the Telegram bot,
     *   facilitating actions like sending messages or executing commands.
     * @return TransitionResult Indicates the outcome of attempting to start an interaction, usually a
     *   `TransitionFailure` with the current state as the next state, implying that no transition has occurred due to
     *   the action being inappropriate or unauthorized in the current state.
     */
    fun onStart(bot: TelegramBot): TransitionResult {
        logger.warn(
            "User ${
                context.username.ifBlank { context.userId.toString() }
            } attempted to start from state ${javaClass.simpleName}")
        return TransitionFailure(this)
    }

    /**
     * Processes text input within this state, providing a response or action based on the input. By default, it logs
     * the processing attempt and returns a success result.
     *
     * @param text The text input to process, which could be null if no input is provided.
     * @param bot A `TelegramBot` instance used to facilitate any bot interactions required by the process.
     * @return BotResult A result object, typically `BotSuccess`, indicating the completion of the processing.
     */
    fun process(text: String?, bot: TelegramBot): BotResult<*> {
        logger.debug("Processing input in state ${javaClass.simpleName}")
        return BotSuccess("Processed input in state ${javaClass.simpleName}", true)
    }

    /**
     * Handles conditions where the system or user remains idle. This might trigger a transition to a designated idle
     * state, effectively managing the user's state when there is no activity.
     *
     * @param bot A `TelegramBot` instance that can be used to send notifications or reminders if necessary.
     * @return TransitionResult The result of transitioning to an idle state, typically a `TransitionSuccess`.
     */
    fun onIdle(bot: TelegramBot): TransitionResult {
        context.state = IdleState(context)
        return TransitionSuccess(context.state)
    }

    fun onRevoke(bot: TelegramBot): TransitionResult {
        logger.warn(
            "User ${context.username.ifBlank { context.userId }} tried to revoke from state ${javaClass.simpleName}"
        )
        return TransitionFailure(this)
    }
}

/**
 * Handles scenarios where a user provides invalid input by sending a clarifying message
 * back to the user through the Telegram bot. This function is used to ensure users are aware
 * of their input mistakes and to guide them towards providing acceptable responses.
 *
 * ## Usage:
 * This function should be called within interaction flows where user inputs are expected to be
 * in specific formats or values, such as during a registration process where the user needs to
 * confirm with 'yes' or 'no'. If the input does not match expected values, this function helps
 * to prompt the user correctly.
 *
 * ### Example 1: Handling Invalid Input
 * ```kotlin
 * if (userInput != "yes" && userInput != "no") {
 *     val result = handleInvalidInput(bot, user)
 *     println(result.message)  // Outputs: "Invalid input. Please type 'yes' or 'no' to confirm or deny registration."
 * }
 * ```
 *
 * @param bot A `TelegramBot` instance through which the message is sent. This allows the function
 *            to interact with the Telegram bot to send messages.
 * @param context A `ReadUser` instance representing the user who provided the invalid input. This
 *                context is used to identify the user and to tailor the message accordingly.
 * @return BotResult The result of the message sending operation, indicating whether the message was
 *         successfully sent or if there was an error during the process.
 */
fun handleInvalidInput(bot: TelegramBot, context: ReadUser): BotResult<*> {
    // Log a warning with the user's identification to trace the source of the invalid input
    logger.warn("Invalid input from user ${context.username.ifBlank { context.userId.toString() }}")

    // Define the message to guide the user on how to provide correct input
    val message = "Invalid input. Please type 'yes' or 'no' to confirm or deny registration."

    // Send the clarifying message to the user and return the result of this operation
    return bot.sendMessage(context, message).fold(
        { BotSuccess("Invalid input message sent", it) },
        { BotFailure("Failed to send invalid input message", it) }
    )
}

/**
 * Verifies if the state of a user in the database matches an expected state after an operation. This function is
 * crucial for ensuring the integrity of state changes within user management workflows.
 *
 * ## Usage:
 * This function should be used after any operation that is supposed to alter a user's state in the database. It checks
 * whether the operation has successfully updated the user's state to the expected value.
 *
 * ### Example:
 * ```kotlin
 * val updateResult = updateUserState(user, "StartState")
 * val verifyResult = verifyUserState(updateResult, "StartState", user)
 * if (verifyResult is BotFailure) {
 *     println("Error: ${verifyResult.message}")
 * }
 * ```
 *
 * @param result
 *  The `BotResult` returned from the previous operation, which is checked to determine if the operation was initially
 *  deemed successful.
 * @param expectedState
 *  The state expected to be set for the user in the database.
 * @param user
 *  The `ReadUser` whose state is being verified. This user's ID is used to check the actual state in the database.
 * @return
 *  Returns the original `BotResult` if the user's state was correctly updated, or a `BotFailure` if the state does not
 *  match the expected value.
 */
fun verifyUserState(result: BotResult<*>, expectedState: String, user: ReadUser): BotResult<*> {
    // Proceed with verification only if the previous result was a success.
    if (result is BotSuccess) {
        // Check if the user's state in the database matches the expected state.
        val isCorrectState = Users.selectAll().where { Users.id eq user.userId }
            .single()[Users.state] == expectedState
        // Return a failure if the state was not updated as expected.
        if (!isCorrectState) return BotFailure("User state was not updated", false)
    }
    // Return the original result if the state was correctly updated, or if the initial operation was not a success.
    return result
}

/**
 * Verifies whether a user has been successfully deleted from the database after an operation that intended to remove
 * them. This function checks the result of the operation and confirms the absence of the user's record in the database.
 *
 * ## Usage:
 * This function is typically called after an attempt to delete a user from the database. It ensures that the deletion
 * was successful by verifying that no records exist for the user. If the user still exists, it returns a `BotFailure`
 * indicating the failure to delete the user.
 *
 * ### Example:
 * ```kotlin
 * val deletionResult = someDeletionFunction(user)
 * val verificationResult = verifyUserDeletion(deletionResult, user)
 * if (verificationResult is BotFailure) {
 *     println("Error: ${verificationResult.message}")
 * }
 * ```
 *
 * @param result The result of the deletion operation, which is checked to determine if further verification is needed.
 * @param user The `ReadUser` whose deletion is being verified. This user's ID is used to check for existing records.
 * @return
 *  Returns the original `BotResult` if the deletion was confirmed, or a `BotFailure` if the user was not successfully
 *  deleted.
 */
fun verifyUserDeletion(result: BotResult<*>, user: ReadUser): BotResult<*> {
    // Check if the operation was initially successful.
    if (result is BotSuccess) {
        val exists = Users.selectAll().where { Users.id eq user.userId }.count() > 0
        // If the user still exists in the database, return a failure result.
        if (exists) return BotFailure("User was not deleted", false)
    }
    // Return the original result if no issues were found.
    return result
}
