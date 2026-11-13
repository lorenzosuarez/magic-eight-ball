package com.example.magiceightball.core.domain.gateway

import kotlinx.coroutines.flow.StateFlow

/** Tells whether AI answers can be offered at all, whatever the user's own choice is. */
interface AiAnswersAvailability {
    /** True while the AI service is switched on for the app and there is a project to ask it through. */
    val isAvailable: StateFlow<Boolean>
}
