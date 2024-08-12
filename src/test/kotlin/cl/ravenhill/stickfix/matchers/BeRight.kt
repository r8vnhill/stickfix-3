package cl.ravenhill.stickfix.matchers

import arrow.core.Either
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should

/**
 * Creates a `Matcher` that checks if an `Either` instance is a `Right`.
 *
 * This function returns a `Matcher` for an `Either` type, which evaluates whether the given `Either` instance is a
 * `Right`. The matcher is primarily used in testing to ensure that a particular operation resulted in a `Right` value,
 * indicating success or an expected right result.
 *
 * @return A `Matcher` that checks if an `Either` is a `Right`.
 */
fun <L, R> beRight() = Matcher<Either<L, R>> { actual ->
    MatcherResult(
        actual.isRight(),
        { "Expected Either to be Right, but was $actual" },
        { "Either should not be Right" }
    )
}

/**
 * Asserts that this `Either` instance is a `Right`.
 *
 * This extension function is used in tests to assert that a particular `Either` value is a `Right`. It uses the
 * `beRight` matcher to perform the assertion. If the `Either` is not a `Right`, the assertion will fail with a
 * descriptive error message. If the assertion passes, the `Either` instance is returned.
 *
 * @receiver The `Either` instance to be checked.
 * @return The original `Either` instance, if it is a `Right`.
 * @throws AssertionError if the `Either` is not a `Right`.
 */
fun <L, R> Either<L, R>.shouldBeRight(): Either<L, R> {
    this should beRight()
    return this
}
