package cl.ravenhill.stickfix.exceptions

import cl.ravenhill.stickfix.states.State

/**
 * An exception that is thrown when a state cannot be resolved from a given name. This exception is used to indicate
 * issues with state resolution in the application.
 *
 * @param name The detail message string explaining the reason for the exception.
 */
class StateResolutionException(name: String) : Exception(
    "State resolution failed for state: $name. Available states: ${State::class.sealedSubclasses.map { it.simpleName }}"
)
