package cl.ravenhill.stickfix.chat

import com.github.kotlintelegrambot.entities.Message

/**
 * The `ReplySticker` class represents a sticker that a user has replied to in a Telegram chat. This class encapsulates
 * the unique file identifier of the sticker, which can be used to reference and manage the sticker within StickFix.
 *
 * @property fileUniqueId The unique identifier for the sticker file, which is used to identify the sticker within the
 *   Telegram API.
 */
class ReplySticker(val fileUniqueId: String) {

    /**
     * A companion object that provides utility methods related to the `ReplySticker` class.
     */
    companion object {

        /**
         * Creates a `ReplySticker` instance from a given `Message` object. This function extracts the unique file
         * identifier of the sticker from the message that the user replied to, and returns a new `ReplySticker`
         * instance if a sticker is present.
         *
         * @param message The `Message` object from which to extract the replied-to sticker.
         * @return A `ReplySticker` instance if the message contains a reply to a sticker, or `null` if no sticker is
         *   present in the replied-to message.
         */
        fun from(message: Message): ReplySticker? =
            message.replyToMessage?.sticker?.fileUniqueId?.let { ReplySticker(it) }
    }
}
