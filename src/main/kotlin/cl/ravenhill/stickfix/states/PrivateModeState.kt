package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.chat.StickfixUser
import org.slf4j.LoggerFactory

/**
 * Represents the state where a user can enable or disable private mode in the Stickfix bot application. This state
 * allows the user to change their privacy settings and handles the appropriate transitions based on the user's input.
 * The `PrivateModeState` class implements the `State` interface, facilitating state-specific actions and transitions.
 *
 * @property user A `StickfixUser` instance representing the user information relevant to the state. This allows the
 *   state to have direct access to and modify user data as necessary during state transitions.
 */
data class PrivateModeState(override val user: StickfixUser) : State() {
    private val logger = LoggerFactory.getLogger(javaClass)
}
