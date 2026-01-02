package com.example.magiceightball.feature.chat

import com.example.magiceightball.core.domain.model.ShakeEvent
import com.example.magiceightball.feature.chat.R

/**
 * Represents the UI state of the chat screen.
 */
data class ChatState(
    val machineState: ChatStateMachine = ChatStateMachine.Idle,
    val shakeStatusRes: Int? = null,
    val titleRes: Int = R.string.title_main,
    val error: String? = null
)

sealed interface ChatStateMachine {
    object Idle : ChatStateMachine
    object Running : ChatStateMachine
    data class Completed(val answer: String) : ChatStateMachine
}

sealed interface ChatIntent {
    data class SendMessage(val text: String) : ChatIntent
}

sealed interface ChatEffect {
    data class ShowToast(val message: String) : ChatEffect
}

data class UiMessage(
    val id: String,
    val text: String,
    val isUser: Boolean
)
