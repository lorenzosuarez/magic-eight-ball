package com.example.magiceightball.core.domain.model

data class QueryConfig(
    val processingDelayMs: Long = 2000L,
    val completionHoldTimeMs: Long = 3000L,
    val cooldownMs: Long = 1000L // Delay before unlocking sensor
)
