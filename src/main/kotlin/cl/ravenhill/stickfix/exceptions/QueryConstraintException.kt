package cl.ravenhill.stickfix.exceptions

import cl.ravenhill.jakt.exceptions.ConstraintException

/**
 * Represents an exception that is thrown when a query constraint is violated. This class extends `ConstraintException`
 * and provides specific handling for query constraint violations.
 *
 * @param message A string representing the error message associated with the constraint violation.
 */
class QueryConstraintException(message: String) : ConstraintException(message)
