package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.db.DatabaseOperationFailure
import cl.ravenhill.stickfix.logInfo
import cl.ravenhill.stickfix.logWarn
import org.slf4j.LoggerFactory


/**
 * Represents the state of a user confirming their start action. This state handles the transition to the idle state
 * after confirming the user's registration and sending the appropriate messages.
 *
 * @property user The user associated with this state.
 */
class StartConfirmationState(override val user: StickfixUser) : State {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the transition of the user to the idle state. This involves checking if the user exists in the database,
     * adding them if they do not, and sending the appropriate messages.
     *
     * @receiver StickfixBot The bot instance used to interact with the Telegram API and database service.
     * @return The result of the transition, indicating success or failure.
     */
    context(StickfixBot)
    override fun onIdle(): TransitionResult = databaseService.getUser(user).fold(
        ifLeft = {
            logWarn(logger) { logUserDataRetrievalFailure(user, it) }
            logInfo(logger) { registeringUserLog(user) }
            databaseService.addUser(user)
            sendMessage(user, WELCOME_MESSAGE)
        },
        ifRight = {
            logInfo(logger) { "Retrieved user data for ${user.debugInfo}: ${it.data}" }
            sendMessage(user, ALREADY_REGISTERED_MESSAGE)
        }
    ).fold(
        ifLeft = { TransitionFailure(this) },
        ifRight = {
            databaseService.setUserState(IdleState(user))
            TransitionSuccess(user.state)
        }
    )
}

private const val WELCOME_MESSAGE = "Welcome to Stickfix!"

private const val ALREADY_REGISTERED_MESSAGE = "You are already registered!"

private fun registeringUserLog(user: StickfixUser) = "Registering new user: ${user.debugInfo}"

private fun logUserDataRetrievalFailure(user: StickfixUser, failure: DatabaseOperationFailure) =
    "Failed to retrieve user data for ${user.debugInfo}: ${failure.message}"
