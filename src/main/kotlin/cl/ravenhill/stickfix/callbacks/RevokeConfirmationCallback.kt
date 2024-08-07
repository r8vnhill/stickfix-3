package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.schema.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
/**
 * Represents a callback handler for confirming or rejecting the revocation of a user's registration. This sealed class
 * defines the common behavior and properties for revocation confirmation callbacks, ensuring type safety and exhaustive
 * handling of all possible confirmation scenarios.
 */
sealed class RevokeConfirmationCallback : CallbackQueryHandler()

/**
 * Handles the confirmation of a user's registration revocation. This data object processes the revocation by deleting
 * the user's information from the database and sending a confirmation message to the user.
 *
 * ## Usage:
 * This class should be invoked when the user confirms the intention to revoke their registration. It ensures that the
 * user's data is removed from the database and that a confirmation message is sent to the user.
 *
 * ### Example 1: Invoking RevokeConfirmationYes
 * ```kotlin
 * val result = RevokeConfirmationYes.invoke(user, bot, dbService)
 * println(result.message)  // Outputs: "Your registration has been revoked."
 * ```
 *
 * @property name The name of the callback handler, derived from the class name.
 */
data object RevokeConfirmationYes : RevokeConfirmationCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Invokes the callback to handle the user's confirmation of revocation. This method deletes the user's data from
     * the database and sends a confirmation message to the user.
     *
     * @param user The `ReadUser` instance representing the user confirming the revocation.
     * @param bot The `TelegramBot` instance used to send messages to the user.
     * @param dbService The `DatabaseService` instance used to interact with the database.
     * @return CallbackResult The result of the callback operation, indicating success or failure.
     */
    override fun invoke(user: ReadUser, bot: TelegramBot, dbService: DatabaseService) = transaction {
        dbService.deleteUser(user)
        Users.deleteWhere { id eq user.userId }
        logger.info("User ${user.username} has been revoked.")
        bot.sendMessage(user, "Your registration has been revoked.").fold(
            ifLeft = {
                logger.info("User ${user.username} has been revoked.")
                CallbackSuccess("Your registration has been revoked.")
            },
            ifRight = {
                logger.error("Failed to send revocation message to user ${user.username}")
                CallbackFailure(it.message)
            }
        )
    }
}

/**
 * Handles the rejection of a user's registration revocation. This data object processes the rejection by logging the
 * user's decision and sending a notification message to the user.
 *
 * ## Usage:
 * This class should be invoked when the user rejects the intention to revoke their registration. It ensures that the
 * user's decision is logged and that a notification message is sent to the user.
 *
 * ### Example 1: Invoking RevokeConfirmationNo
 * ```kotlin
 * val result = RevokeConfirmationNo.invoke(user, bot, dbService)
 * println(result.message)  // Outputs: "Your registration has not been revoked."
 * ```
 *
 * @property name The name of the callback handler, derived from the class name.
 */
data object RevokeConfirmationNo : RevokeConfirmationCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Invokes the callback to handle the user's rejection of revocation. This method logs the user's decision and sends
     * a notification message to the user.
     *
     * @param user The `ReadUser` instance representing the user rejecting the revocation.
     * @param bot The `TelegramBot` instance used to send messages to the user.
     * @param dbService The `DatabaseService` instance used to interact with the database.
     * @return CallbackResult The result of the callback operation, indicating success or failure.
     */
    override fun invoke(user: ReadUser, bot: TelegramBot, dbService: DatabaseService): CallbackResult {
        logger.info("User ${user.username} has chosen not to revoke.")
        return bot.sendMessage(user, "Your registration has not been revoked.").fold(
            ifLeft = {
                logger.info("User ${user.username} has chosen not to revoke.")
                CallbackSuccess("Your registration has not been revoked.")
            },
            ifRight = {
                logger.error("Failed to send revocation rejection message to user ${user.username}")
                CallbackFailure(it.message)
            }
        )
    }
}
