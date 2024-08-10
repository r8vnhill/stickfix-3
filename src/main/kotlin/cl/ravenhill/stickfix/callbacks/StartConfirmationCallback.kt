/*
 * Copyright (c) 2024, Ignacio Slater M.
 * 2-Clause BSD License.
 */

package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.StartConfirmationNo.name
import cl.ravenhill.stickfix.callbacks.StartConfirmationYes.name
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.DatabaseOperationFailure
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.states.TransitionSuccess

/**
 * Represents a handler for processing start confirmation callback queries in the Stickfix bot application. This sealed
 * class extends the `CallbackQueryHandler` class, providing additional functionality for sending messages to users.
 * Subclasses must define the specific logic for handling start confirmation queries by implementing the abstract
 * properties and methods from `CallbackQueryHandler`.
 *
 * @property logger A logger instance for logging actions related to callback query handling. This logger is used to
 *   record activities such as processing queries and handling errors.
 */
sealed class StartConfirmationCallback : CallbackQueryHandler() {

    context(StickfixBot) override fun handleUserRegistered(user: StickfixUser): CallbackResult {
        logInfo(logger) { "User ${user.debugInfo} is already registered." }
        return CallbackFailure(alreadyRegisteredMessage(user))
    }

    /**
     * Retrieves a user from the temporary database. This function attempts to find the user in the temporary database
     * and handles the result accordingly. If the user is found, it processes the user data; if not, it handles the
     * failure.
     *
     * @param user The `StickfixUser` instance representing the user to be retrieved from the temporary database.
     * @return A `CallbackResult` indicating the outcome of the retrieval process, which could either be successful
     *   processing of the user data or handling a failure to retrieve the user.
     */
    context(StickfixBot)
    protected fun retrieveTempUser(user: StickfixUser): CallbackResult = tempDatabase.getUser(user.id).fold(
        ifLeft = { handleTempUserRetrievalFailure(it) },
        ifRight = { handleTempUserRetrievalSuccess(user, it.data) }
    )

    /**
     * Handles the failure to retrieve a user from the temporary database. This function logs the error and returns a
     * `CallbackFailure` result with the error message.
     *
     * @param error The `DatabaseOperationFailure` instance representing the failure that occurred during the retrieval
     *   attempt.
     * @return A `CallbackFailure` result containing the error message.
     */
    private fun handleTempUserRetrievalFailure(error: DatabaseOperationFailure): CallbackResult {
        logError(logger) { "Failed to retrieve user data from temporary database: $error" }
        return CallbackFailure(error.toString())
    }

    /**
     * Handles the successful retrieval of a user from the temporary database. This function processes the user data and
     * returns the appropriate `CallbackResult`.
     *
     * @param user The `StickfixUser` instance representing the current user.
     * @param tempUser The `StickfixUser` instance retrieved from the temporary database.
     * @return A `CallbackResult` indicating the success or failure of the user's retrieval from the temporary database.
     */
    context(StickfixBot)
    protected abstract fun handleTempUserRetrievalSuccess(user: StickfixUser, tempUser: StickfixUser): CallbackResult
}

/**
 * Handles the affirmative response to a start confirmation query in the Stickfix bot application. This object extends
 * `StartConfirmationCallback`, applying specific logic for users who confirm a start action. It checks if the user is
 * already registered and either registers them or notifies them of their current status.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object StartConfirmationYes : StartConfirmationCallback() {

    override val name = this::class.simpleName!!

    /**
     * Handles the scenario where a user is not registered in the main database. This function logs an informational
     * message indicating that the user is not registered and proceeds to retrieve the user data from the temporary
     * database for further processing.
     *
     * @param user The `StickfixUser` instance representing the user that is not registered in the main database.
     * @return A `CallbackResult` that indicates the outcome of the process, which includes either successfully
     *   retrieving the user from the temporary database or handling any errors that occur during the process.
     */
    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logInfo(logger) { "User ${user.debugInfo} is not registered. Registering..." }
        return retrieveTempUser(user)
    }

    /**
     * Handles the successful retrieval of a user from the temporary database. This function attempts to confirm the
     * start process for the temporary user and returns the appropriate `CallbackResult`.
     *
     * @param user The `StickfixUser` instance representing the current user.
     * @param tempUser The `StickfixUser` instance retrieved from the temporary database.
     * @return A `CallbackResult` indicating the success or failure of the user's transition to a confirmed state.
     */
    context(StickfixBot)
    override fun handleTempUserRetrievalSuccess(user: StickfixUser, tempUser: StickfixUser): CallbackResult =
        when (val result = tempUser.onStartConfirmation()) {
            is TransitionSuccess -> sendWelcomeMessage(user, result)
            else -> CallbackFailure(result.toString())
        }

    /**
     * Sends a welcome message to the user after a successful transition to a confirmed state. This function attempts to
     * send a predefined welcome message to the user and returns the appropriate `CallbackResult` based on the success
     * or failure of the message sending operation.
     *
     * @param user The `StickfixUser` instance representing the recipient of the welcome message.
     * @param result The `TransitionSuccess` result indicating that the user's transition was successful.
     * @return A `CallbackResult` indicating whether the welcome message was sent successfully or if there was a
     *   failure.
     */
    context(StickfixBot)
    private fun sendWelcomeMessage(user: StickfixUser, result: TransitionSuccess): CallbackResult {
        return sendMessage(user, welcomeMessage).fold(
            ifLeft = { CallbackFailure(it.toString()) },
            ifRight = { CallbackSuccess(result.toString()) }
        )
    }
}

/**
 * Handles the negative response to a start confirmation query in the Stickfix bot application. This object extends
 * `StartConfirmationCallback`, applying specific logic for users who decline a start action. It sends a message to the
 * user confirming their choice and logs the action.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object StartConfirmationNo : StartConfirmationCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Handles the successful retrieval of a temporary user's data when the user chooses not to register. This function
     * processes the user's rejection of the start command and sends a message confirming their decision.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @param tempUser The temporary user data retrieved from the database.
     * @return `CallbackResult` indicating the outcome of the rejection handling process.
     */
    context(StickfixBot)
    override fun handleTempUserRetrievalSuccess(user: StickfixUser, tempUser: StickfixUser): CallbackResult =
        when (val result = tempUser.onStartRejection()) {
            is TransitionSuccess -> sendRegisterLaterMessage(user, result)
            else -> CallbackFailure(result.toString())
        }

    /**
     * Handles the case when a user who is not registered chooses not to register. This function logs the action and
     * skips the registration process.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @return `CallbackResult` indicating the outcome of the user's decision not to register.
     */
    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logInfo(logger) { "User ${user.debugInfo} is not registered. Skipping registration." }
        return retrieveTempUser(user)
    }

    /**
     * Sends a message to the user confirming their decision not to register and logs the successful transition.
     *
     * @param user The `StickfixUser` instance representing the user.
     * @param result The `TransitionSuccess` result indicating that the user's transition to a rejection state was
     *   successful.
     * @return `CallbackResult` indicating whether the message was sent successfully or if there was a failure.
     */
    context(StickfixBot)
    private fun sendRegisterLaterMessage(user: StickfixUser, result: TransitionSuccess): CallbackResult =
        sendMessage(user, "You have chosen not to register. Remember you can always register later!").fold(
            ifLeft = { CallbackFailure(it.toString()) },
            ifRight = { CallbackSuccess(result.toString()) }
        )
}

/**
 * A constant string that serves as the welcome message for new users when they register with the Stickfix bot.
 */
private val welcomeMessage = """
    |Welcome to Stickfix!
    |For a list of available commands, type /help.
""".trimMargin()

/**
 * Generates a message indicating that the user is already registered with the Stickfix bot.
 *
 * @param user The user for whom the message is being generated.
 * @return A string message stating that the user is already registered.
 */
private fun alreadyRegisteredMessage(user: StickfixUser) = "User ${user.debugInfo} is already registered."
