package cl.ravenhill.stickfix

import cl.ravenhill.jakt.constraints.Constraint
import cl.ravenhill.stickfix.exceptions.QueryConstraintException
import org.jetbrains.exposed.sql.Query

/**
 * Represents a constraint that validates if a query result has a specific size. This class implements the `Constraint`
 * interface for `Query` types, ensuring that the number of results in the query matches the expected size.
 *
 * @param predicate The predicate function that determines if the query result size matches the expected size.
 */
class HaveSize(private val predicate: (Long) -> Boolean) : Constraint<Query> {
    /**
     * The validator function that checks if the query result size matches the expected size.
     */
    override val validator: (Query) -> Boolean = { query ->
        predicate(query.count())
    }

    /**
     * Generates a `QueryConstraintException` with the provided description when the constraint is violated.
     *
     * @param description The description of the constraint violation.
     * @return `QueryConstraintException` with the provided description.
     */
    override fun generateException(description: String) = QueryConstraintException(description)
}
