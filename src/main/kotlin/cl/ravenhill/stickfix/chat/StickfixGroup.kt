package cl.ravenhill.stickfix.chat

import com.github.kotlintelegrambot.entities.Chat

/**
 * Represents a group with an identifier and an optional title.
 *
 * @property id The unique identifier of the group.
 * @property title The title of the group, which may be null.
 */
class StickfixGroup(override val id: Long, private val title: String?) : StickfixChat {

    /**
     * Provides debug information for the group.
     *
     * @return A string representing the debug information. If the title is not null, it returns the title enclosed in
     *   single quotes. Otherwise, it returns the group's unique identifier.
     */
    override val debugInfo = title?.let { "'$it'" } ?: id.toString()

    companion object {
        /**
         * Creates a [StickfixGroup] instance from a [Chat] object.
         *
         * @param chat The [Chat] object containing the information to create a [StickfixGroup].
         * @return A new [StickfixGroup] instance with the id and title from the provided [Chat].
         */
        fun from(chat: Chat) = StickfixGroup(chat.id, chat.title)
    }
}
