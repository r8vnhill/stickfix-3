package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.PrivateMode
import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.PrivateModeDisabledCallback.name
import cl.ravenhill.stickfix.callbacks.PrivateModeEnabledCallback.name
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.DatabaseOperationSuccess

/**
 * Represents a base class for handling private mode-related callbacks in the Stickfix bot application. This sealed
 * class provides a foundation for specific private mode callbacks, ensuring that all potential callbacks are known at
 * compile time, allowing for exhaustive checking of callback types.
 */
sealed class PrivateModeCallback : CallbackQueryHandler()

/**
 * Handles the callback for enabling private mode in the Stickfix bot application. This object extends
 * `PrivateModeCallback`, applying specific logic for users who enable private mode. It updates the user's private mode
 * setting in the database and sends a confirmation message.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object PrivateModeEnabledCallback : PrivateModeCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Handles the enabling of private mode by updating the user's private mode setting in the database and sending a
     * confirmation message.
     *
     * @param user The `StickfixUser` instance representing the user who enabled private mode.
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return CallbackResult The result of enabling private mode, indicating success or failure.
     */
    override fun invoke(user: StickfixUser, bot: StickfixBot) =
        bot.databaseService.setPrivateMode(user, PrivateMode.ENABLED).fold(
            ifLeft = { CallbackFailure("Failed to enable private mode.") },
            ifRight = { CallbackSuccess("Private mode enabled.") },
        )
}

/**
 * Handles the callback for disabling private mode in the Stickfix bot application. This object extends
 * `PrivateModeCallback`, applying specific logic for users who disable private mode. It updates the user's private mode
 * setting in the database and sends a confirmation message.
 *
 * @property name The simple name of the class, used for logging and reference within the system.
 */
data object PrivateModeDisabledCallback : PrivateModeCallback() {
    override val name: String = this::class.simpleName!!

    /**
     * Handles the disabling of private mode by updating the user's private mode setting in the database and sending a
     * confirmation message.
     *
     * @param user The `StickfixUser` instance representing the user who disabled private mode.
     * @param bot The `StickfixBot` instance used to send messages to the user.
     * @return CallbackResult The result of disabling private mode, indicating success or failure.
     */
    override fun invoke(user: StickfixUser, bot: StickfixBot) =
        when (bot.databaseService.setPrivateMode(user, PrivateMode.DISABLED)) {
            is DatabaseOperationSuccess<*> -> CallbackSuccess("Private mode disabled.")
            else -> CallbackFailure("Failed to disable private mode.")
        }
}
