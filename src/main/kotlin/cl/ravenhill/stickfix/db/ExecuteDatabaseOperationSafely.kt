package cl.ravenhill.stickfix.db

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import cl.ravenhill.jakt.exceptions.CompositeException
import cl.ravenhill.stickfix.exceptions.DatabaseOperationException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

/**
 * Executes a database operation within a transaction and handles any potential exceptions that may occur during the
 * operation. This function ensures that any database operation is executed safely, returning a result indicating
 * success or failure.
 *
 * @param T The type of the result produced by the successful database operation.
 * @param database The `Database` instance used to execute the database operation.
 * @param successful A lambda function representing the successful database operation to be executed within the
 *   transaction context.
 * @return The result of the database operation, wrapped in an `Either` type to indicate success or failure. On
 *   success, it returns a `DatabaseOperationSuccess` with a success message and the result of the operation. On
 *   failure, it returns a `DatabaseOperationFailure` with the appropriate error message and exception.
 */
fun <T> executeDatabaseOperationSafely(
    database: Database,
    successful: Transaction.() -> T,
): Either<DatabaseOperationFailure, DatabaseOperationSuccess<T>> = transaction(database) {
    try {
        DatabaseOperationSuccess("Database operation completed successfully.", successful()).right()
    } catch (e: SQLException) {
        handleDatabaseException(e).left()
    } catch (e: CompositeException) {
        handleDatabaseException(e).left()
    }
}

/**
 * Handles the exceptions that occur during the database operation, creating a `DatabaseOperationFailure`.
 *
 * @param e The exception that was thrown during the database operation.
 * @return `DatabaseOperationFailure` with an appropriate error message and exception.
 */
private fun handleDatabaseException(
    e: Exception,
) = DatabaseOperationFailure(
    "Database operation failed.",
    DatabaseOperationException(e.message ?: "Unknown error.")
)
