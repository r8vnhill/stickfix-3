package cl.ravenhill.stickfix

import cl.ravenhill.jakt.constraints.Constraint
import cl.ravenhill.jakt.exceptions.ConstraintException

/**
 * Represents a constraint that checks if an object is null.
 */
data object BeNull : Constraint<Any?> {
    override val validator: (Any?) -> Boolean = { it == null }

    override fun generateException(description: String) = ConstraintException(description)
}
