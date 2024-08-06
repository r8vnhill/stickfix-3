package cl.ravenhill.stickfix

import com.github.kotlintelegrambot.types.TelegramBotResult

class MessageSendingError(message: String) : Exception(message) {
    companion object {
        fun from(telegramError: TelegramBotResult.Error<*>) =
            MessageSendingError("Error sending message: $telegramError")
    }
}
