package cl.ravenhill.stickfix.states

import cl.ravenhill.stickfix.chat.StickfixUser
import cl.ravenhill.stickfix.exceptions.StateResolutionException

/**
 * Resolves the state from the given name by matching it against the sealed subclasses of the `State` interface.
 * This function looks for a state with a simple name that matches the provided name (case-insensitive) and returns
 * the corresponding state instance, constructed with the provided `StickfixUser`. If no matching state is found, it
 * throws a `StateResolutionException`.
 *
 * @param name The name of the state to resolve. This should be the simple name of one of the sealed subclasses of
 *   `State`.
 * @param user The `StickfixUser` instance to be passed as a parameter to the state constructor.
 * @return The resolved state instance that matches the provided name, constructed with the provided user.
 * @throws StateResolutionException If no matching state is found for the given name.
 */
fun resolveState(name: String, user: StickfixUser) = State::class.sealedSubclasses
    .firstOrNull { klass ->
        klass.simpleName?.equals(name, ignoreCase = true) ?: false
    }
    ?.constructors?.first()
    ?.call(user)
    ?: throw StateResolutionException(name)
