package cl.ravenhill.stickfix.matchers

import arrow.core.Either
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should

/**
 * Creates a `Matcher` that checks if an `Either` instance is a `Left`.
 *
 * This function returns a `Matcher` for an `Either` type, which evaluates whether the given `Either` instance is a
 * `Left`. The matcher is primarily used in testing to ensure that a particular operation resulted in a `Left` value,
 * indicating failure or an expected left result.
 *
 * @return A `Matcher` that checks if an `Either` is a `Left`.
 */
fun <L, R> beLeft() = Matcher<Either<L, R>> {
    MatcherResult(
        it.isLeft(),
        { "Expected Either to be Left, but was Right" },
        { "Expected Either to be Right, but was Left" }
    )
}

/**
 * Asserts that this `Either` instance is a `Left`.
 *
 * This extension function is used in tests to assert that a particular `Either` value is a `Left`. It uses the `beLeft`
 * matcher to perform the assertion. If the `Either` is not a `Left`, the assertion will fail with a descriptive error
 * message.
 *
 * @receiver The `Either` instance to be checked.
 * @throws AssertionError if the `Either` is not a `Left`.
 */
fun <L, R> Either<L, R>.shouldBeLeft(): Either<L, R> {
    this should beLeft()
    return this
}

