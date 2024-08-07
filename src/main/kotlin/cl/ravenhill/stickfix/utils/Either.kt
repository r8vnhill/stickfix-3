package cl.ravenhill.stickfix.utils

import arrow.core.Either

/**
 * Flattens an `Either` instance by extracting the contained value regardless of whether it is in
 * the left or right position. This function simplifies the handling of `Either` instances where
 * the contained value is the same type for both left and right.
 *
 * @param T The type of the value contained in the `Either`.
 * @return The value contained in the `Either`, regardless of whether it is in the left or right position.
 */
fun <T> Either<T, T>.flatten(): T = fold({ it }, { it })
