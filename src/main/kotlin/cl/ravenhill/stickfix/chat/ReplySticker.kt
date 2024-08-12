package cl.ravenhill.stickfix.chat

class ReplySticker(val fileUniqueId: String) {
    companion object {
        fun from(message: Message): Either<Error, ReplySticker> {
            val sticker = message.sticker ?: return Error("Message does not contain a sticker").left()
            return ReplySticker(sticker.fileId, sticker.fileUniqueId).right()
        }
    }
}