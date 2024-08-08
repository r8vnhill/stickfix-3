package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.bot.StickfixBot
import cl.ravenhill.stickfix.chat.StickfixUser

/**
 * Represents the state where a user has rejected the start command. This state handles the transition to the idle state
 * when the user chooses not to register. It sends a message to the user indicating that they have chosen not to
 * register and that they can register later.
 *
 * @property user The `StickfixUser` instance representing the user in this state.
 */
class StartRejectionState(override val user: StickfixUser) : State {

    /**
     * Handles the transition to the idle state. This function sends a message to the user indicating that they have
     * chosen not to register, and then updates the user's state to `IdleState`.
     *
     * @receiver The `StickfixBot` instance used to send messages and manage the database service.
     * @return `TransitionResult` indicating the result of the transition to the idle state.
     */
    context(StickfixBot)
    override fun onIdle(): TransitionResult = sendMessage(
        user,
        "You have chosen not to register. Remember you can always register later!"
    ).fold(
        ifLeft = { TransitionFailure(this) },
        ifRight = {
            databaseService.setUserState(IdleState(user))
            TransitionSuccess(user.state)
        }
    )
}
