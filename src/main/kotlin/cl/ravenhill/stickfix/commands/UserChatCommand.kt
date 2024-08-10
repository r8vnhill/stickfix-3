package cl.ravenhill.stickfix.commands

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser

/**
 * Represents a base class for user-specific chat commands in the Stickfix bot. This sealed class extends the
 * `Command` class and provides a common structure for commands that operate on user-specific data, such as retrieving
 * or modifying a user's information in the database. It enforces the implementation of specific behavior when the user
 * is either registered or not registered in the system.
 *
 * The class ensures that commands handle both scenarios (user registered and user not registered) by requiring the
 * implementation of abstract methods for each case. These commands typically interact with the bot's database
 * service to retrieve user data and perform necessary actions based on the user's status.
 */
sealed class UserChatCommand : Command() {

    /**
     * Invokes the command by determining whether the user is registered in the database. It retrieves the user's data
     * from the database and applies the appropriate behavior based on whether the user is found.
     *
     * @param user The `StickfixUser` on which the command is being invoked.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return `CommandResult` indicating the outcome of the command, which could be either a success or failure,
     *   depending on whether the user is registered or not.
     */
    context(StickfixBot)
    operator fun invoke(user: StickfixUser): CommandResult = databaseService.getUser(user).fold(
        ifLeft = { handleUserNotRegistered(user) },
        ifRight = { handleUserRegistered(user) }
    )

    /**
     * Defines the behavior of the command when the user is found in the database. This method must be implemented by
     * subclasses to specify what action to take when the user is registered.
     *
     * @param user The `StickfixUser` instance representing the registered user.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return `CommandResult` indicating the outcome of handling the registered user, typically a success or failure
     *   depending on the command's execution.
     */
    context(StickfixBot)
    protected abstract fun handleUserRegistered(user: StickfixUser): CommandResult

    /**
     * Defines the behavior of the command when the user is not found in the database. This method must be implemented
     * by subclasses to specify what action to take when the user is not registered.
     *
     * @param user The `StickfixUser` instance representing the unregistered user.
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and the database service.
     * @return `CommandResult` indicating the outcome of handling the unregistered user, typically a success or failure
     *   depending on the command's execution.
     */
    context(StickfixBot)
    protected abstract fun handleUserNotRegistered(user: StickfixUser): CommandResult
}
