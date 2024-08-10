package cl.ravenhill.stickfix.callbacks

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.callbacks.PrivateModeDisabledCallback.name
import cl.ravenhill.stickfix.callbacks.PrivateModeEnabledCallback.name
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.states.TransitionFailure
import cl.ravenhill.stickfix.states.TransitionSuccess

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
     * Processes the callback for enabling private mode for a registered user. It updates the user's private mode
     * setting in the database and sends a confirmation message. If the transition to private mode is successful, the
     * user is notified. If the transition fails, an error message is logged, and a failure result is returned.
     *
     * @param user The `StickfixUser` instance representing the user who initiated the private mode enablement callback.
     * @return `CallbackResult` indicating the success or failure of enabling private mode for the user.
     */
    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CallbackResult {
        return when (val result = user.onPrivateModeEnabled()) {
            is TransitionSuccess -> {
                logInfo(logger) { "User ${user.username} enabled private mode." }
                sendPrivateModeEnabledMessage(user)
            }
            is TransitionFailure -> {
                logError(logger) { "Failed to enable private mode for user ${user.username}: $result" }
                CallbackFailure("Failed to enable private mode for user ${user.debugInfo}.")
            }
        }
    }

    /**
     * Sends a confirmation message to the user after successfully enabling private mode. The message informs the user
     * that private mode has been enabled and provides details on its functionality.
     *
     * @param user The `StickfixUser` instance representing the user who has enabled private mode.
     * @return `CallbackResult` indicating the success or failure of sending the private mode enabled message to the
     *   user.
     */
    context(StickfixBot)
    private fun sendPrivateModeEnabledMessage(user: StickfixUser): CallbackResult {
        val message = "You have enabled private mode. You have access to your private stickers and every sticker you " +
                "add will be private. You can disable private mode at any time."
        return sendMessage(user, message).fold(
            ifLeft = { CallbackFailure("Failed to send private mode enabled message to user ${user.debugInfo}.") },
            ifRight = { CallbackSuccess("Private mode enabled for user ${user.debugInfo}.") },
        )
    }

    /**
     * Handles the scenario where a user who is not registered attempts to enable private mode. This function logs an
     * error message and returns a failure result indicating that the operation cannot proceed because the user is not
     * registered.
     *
     * @param user The `StickfixUser` instance representing the unregistered user attempting to enable private mode.
     * @return `CallbackResult` indicating failure to enable private mode due to the user not being registered.
     */
    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logError(logger) { "User ${user.username} is not registered. Cannot enable private mode." }
        return CallbackFailure("User is not registered.")
    }
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

    context(StickfixBot)
    override fun handleUserRegistered(user: StickfixUser): CallbackResult {
        return when (val result = user.onPrivateModeDisabled()) {
            is TransitionSuccess -> {
                logInfo(logger) { "User ${user.username} disabled private mode." }
                sendPrivateModeDisabledMessage(user)
            }
            is TransitionFailure -> {
                logError(logger) { "Failed to disable private mode for user ${user.username}: $result" }
                CallbackFailure("Failed to disable private mode for user ${user.debugInfo}.")
            }
        }
    }

    context(StickfixBot)
    private fun sendPrivateModeDisabledMessage(user: StickfixUser): CallbackResult {
        val message = "You have disabled private mode. All stickers you add will now be public. You can enable " +
                "private mode at any time."
        return sendMessage(user, message).fold(
            ifLeft = { CallbackFailure("Failed to send private mode disabled message to user ${user.debugInfo}.") },
            ifRight = { CallbackSuccess("Private mode disabled for user ${user.debugInfo}.") },
        )
    }

    context(StickfixBot)
    override fun handleUserNotRegistered(user: StickfixUser): CallbackResult {
        logError(logger) { "User ${user.username} is not registered. Cannot disable private mode." }
        return CallbackFailure("User is not registered.")
    }
}
