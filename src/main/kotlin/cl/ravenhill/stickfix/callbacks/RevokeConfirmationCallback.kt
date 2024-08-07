package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.bot.TelegramBot
import cl.ravenhill.stickfix.chat.ReadUser
import cl.ravenhill.stickfix.db.DatabaseService
import cl.ravenhill.stickfix.db.StickfixDatabase
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

data object RevokeConfirmationYes : RevokeConfirmationCallback() {
    override val name: String = this::class.simpleName!!
    override fun invoke(user: ReadUser, bot: StickfixBot, dbService: StickfixDatabase) = transaction {
        dbService.deleteUser(user)
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

data object RevokeConfirmationNo : RevokeConfirmationCallback() {
    override val name: String = this::class.simpleName!!
    override fun invoke(user: ReadUser, bot: StickfixBot, dbService: StickfixDatabase) = transaction {
        logger.info("User ${user.username} has chosen not to revoke.")
        bot.sendMessage(user, "Your registration has not been revoked.").fold(
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
