package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo.name
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes.name
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.states.IdleState
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionSuccess

/**
 * Represents a callback handler for confirming or rejecting the revocation of a user's registration. This sealed class
 * defines the common behavior and properties for revocation confirmation callbacks, ensuring type safety and exhaustive
 * handling of all possible confirmation scenarios.
 */
sealed class RevokeConfirmationCallback : CallbackQueryHandler()

/**
 * Handles the confirmation of user revocation in the Stickfix bot application. This object extends
 * `RevokeConfirmationCallback`, applying specific logic for users who confirm their revocation. It deletes the user
 * from the database and sends a confirmation message.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object RevokeConfirmationYes : RevokeConfirmationCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Handles the scenario where a user is registered in the main database and confirms their revocation. This function
     * attempts to delete the user's record from the database. If successful, it logs the revocation and returns a
     * `CallbackSuccess` result; otherwise, it logs the error and returns a `CallbackFailure`.
     *
     * @param user The `StickfixUser` instance representing the user who initiated the revocation confirmation.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the database operations.
     * @return `CallbackResult` The result of processing the revocation confirmation, indicating either success or
     *   failure in deleting the user from the database.
     */
    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CallbackResult =
        when (val result = user.onRevokeConfirmation()) {
            is TransitionSuccess -> {
                logInfo(logger) { "User ${user.username} revoked successfully." }
                CallbackSuccess("User revoked.")
            }

            is TransitionFailure -> {
                logError(logger) { "Failed to revoke user ${user.username}: $result" }
                CallbackFailure("Failed to revoke user.")
            }
        }

    /**
     * Handles the scenario where a user is not registered in the main database but attempts to confirm revocation. This
     * function logs an error indicating that the user is not registered and returns a `CallbackFailure` result.
     *
     * @param user The `StickfixUser` instance representing the user who attempted the revocation confirmation.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and manage the database operations.
     * @return `CallbackResult` The result of the revocation confirmation attempt, which is always a failure in this
     *   case since the user is not registered.
     */
    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logError(logger) { "User ${user.username} is not registered. Cannot confirm revocation." }
        return CallbackFailure("User is not registered.")
    }
}

/**
 * Handles the rejection of user revocation in the Stickfix bot application. This object extends
 * `RevokeConfirmationCallback`, applying specific logic for users who reject their revocation. It retains the user's
 * registration and sends a confirmation message.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object RevokeConfirmationNo : RevokeConfirmationCallback() {
    override val name: String = this::class.simpleName!!
    context(StickfixBot) override fun handleUserRegistered(user: StickfixUser): CallbackResult {
        databaseService.setUserState(user, ::IdleState).fold(
            ifLeft = {
                logError(logger) { "Failed to set user ${user.username} state to Idle: $it" }
                return CallbackFailure("Failed to set user state.")
            },
            ifRight = {
                logInfo(logger) { "User ${user.username} retained registration." }
                return CallbackSuccess("User retained registration.")
            }
        )
    }

    context(StickfixBot) override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logError(logger) { "User ${user.username} is not registered. Cannot reject revocation." }
        return CallbackFailure("User is not registered.")
    }
}
