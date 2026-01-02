package com.example.magiceightball.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val index: Int,
    val message: String
)

@Serializable
data class Magic8BallDataDto(
    val en: List<MessageDto>,
    val es: List<MessageDto>
)
