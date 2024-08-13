package cl.ravenhill.stickfix

import arrow.core.Either


/**
 * Returns the `Right` value of an `Either` if it exists, or `null` if the `Either` is `Left`.
 *
 * This extension function is used to extract the `Right` value from an `Either` if it is present, returning it
 * directly. If the `Either` is a `Left`, the function returns `null`.
 *
 * @receiver The `Either` instance from which to extract the `Right` value.
 * @return The `Right` value if present, or `null` if the `Either` is `Left`.
 */
internal fun <A, B> Either<A, B>.rightOrNull(): B? = fold({ null }, { it })
