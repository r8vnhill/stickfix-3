package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationNo.name
import cl.ravenhill.stickfix.callbacks.RevokeConfirmationYes.name
import cl.ravenhill.stickfix.db.StickfixDatabase
import cl.ravenhill.stickfix.error
import cl.ravenhill.stickfix.info
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
     * @param user The `ReadUser` instance representing the user who confirmed the revocation.
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return CallbackResult The result of the revocation confirmation, indicating success or failure.
     */
    override fun invoke(user: ReadUser, bot: StickfixBot): CallbackResult {
        bot.databaseService.deleteUser(user)
        info(logger) { "User ${user.username} has been revoked." }
        return bot.sendMessage(user, "Your registration has been revoked.").fold(
            ifLeft = {
                info(logger) { "User ${user.username} has been revoked." }
                CallbackSuccess("Your registration has been revoked.")
            },
            ifRight = {
                error(logger) { "Failed to send revocation message to user ${user.username}" }
                CallbackFailure(it.message)
            }
        )
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

    /**
     * Handles the revocation rejection by retaining the user's registration and sending a confirmation message.
     *
     * @param user The `ReadUser` instance representing the user who rejected the revocation.
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @param databaseService The `StickfixDatabase` instance for accessing and updating user data.
     * @return CallbackResult The result of the revocation rejection, indicating success or failure.
     */
    override fun invoke(user: ReadUser, bot: StickfixBot) = transaction {
        info(logger) { "User ${user.username} has chosen not to revoke." }
        bot.sendMessage(user, "Your registration has not been revoked.").fold(
            ifLeft = {
                info(logger) { "User ${user.username} has chosen not to revoke." }
                CallbackSuccess("Your registration has not been revoked.")
            },
            ifRight = {
                error(logger) { "Failed to send revocation rejection message to user ${user.username}" }
                CallbackFailure(it.message)
            }
        )
    }
}
