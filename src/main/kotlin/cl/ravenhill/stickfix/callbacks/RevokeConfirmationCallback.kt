package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo.name
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes.name
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import org.jetbrains.exposed.sql.transactions.transaction

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
     * Handles the revocation confirmation by deleting the user from the database and sending a confirmation message.
     *
     * @param user The `StickfixUser` instance representing the user who confirmed the revocation.
     * @return CallbackResult The result of the revocation confirmation, indicating success or failure.
     */
    context(StickfixBot)
    override fun invoke(user: StickfixUser): CallbackResult = databaseService.deleteUser(user).fold(
        ifLeft = {
            logError(logger) { "Failed to revoke user ${user.debugInfo}" }
            CallbackFailure(it.message)
        },
        ifRight = {
            logInfo(logger) { "User ${user.username} has been revoked." }
            sendMessage(user, "Your registration has been revoked.").fold(
                ifLeft = {
                    logInfo(logger) { "User ${user.username} has been revoked." }
                    CallbackSuccess("Your registration has been revoked.")
                },
                ifRight = {
                    logError(logger) { "Failed to send revocation message to user ${user.username}" }
                    CallbackFailure(it.message)
                }
            )
        }
    )
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

    /**
     * Handles the revocation rejection by retaining the user's registration and sending a confirmation message.
     *
     * @param user The `StickfixUser` instance representing the user who rejected the revocation.
     * @return CallbackResult The result of the revocation rejection, indicating success or failure.
     */
    context(StickfixBot)
    override fun invoke(user: StickfixUser): CallbackResult {
        logInfo(logger) { "User ${user.username} has chosen not to revoke." }
        return sendMessage(user, "Your registration has not been revoked.").fold(
            ifLeft = {
                logInfo(logger) { "User ${user.username} has chosen not to revoke." }
                CallbackSuccess("Your registration has not been revoked.")
            },
            ifRight = {
                logError(logger) { "Failed to send revocation rejection message to user ${user.username}" }
                CallbackFailure(it.message)
            }
        )
    }
}
