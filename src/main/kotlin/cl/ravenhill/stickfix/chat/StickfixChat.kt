package cl.ravenhill.stickfix.chat

/**
 * Represents a chat within the Stickfix bot application. This sealed interface defines the essential properties
 * that all chat types in Stickfix must have. By sealing this interface, it ensures that all implementations are
 * known at compile time, providing a type-safe way to handle different types of chats within the bot.
 *
 * @property id The unique identifier for the chat. This ID is used to identify and distinguish the chat within
 *   Stickfix.
 * @property debugInfo A string representation of the chat for debugging purposes. This string is used to provide
 *  additional information about the chat when logging or debugging the application.
 */
sealed interface StickfixChat {

    val id: Long

    val debugInfo: String
}
