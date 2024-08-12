package cl.ravenhill.stickfix.states.base

import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.logError
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The `StateBase` interface defines the foundational contract for all state classes within StickFix. It ensures that
 * each state is associated with a specific `StickfixUser`, allowing state-specific logic to operate with user context.
 *
 * @property user The `StickfixUser` instance representing the user associated with this state.
 */
interface State {
    val user: StickfixUser
}

/**
 * The `AbstractStateBase` class provides a base implementation of the `StateBase` interface. It serves as a common
 * superclass for all concrete state classes, offering shared functionality that can be reused across different states.
 * This class ensures that each state has access to a logger for recording state-related activities such as transitions
 * and errors.
 */
abstract class AbstractState(override val user: StickfixUser) : State {

    /**
     * A logger instance for logging state-related actions. This logger is private to the state and is used to record
     * activities such as transitions and errors.
     */
    protected val logger: Logger get() = LoggerFactory.getLogger(javaClass)

    /**
     * Logs an error message for unauthorized or invalid state transitions.
     *
     * @param action The action that was attempted, leading to the transition failure.
     */
    protected fun logTransitionFailure(action: String) =
        logError(logger) { "User ${user.debugInfo} attempted to $action from state ${javaClass.simpleName}" }
}
