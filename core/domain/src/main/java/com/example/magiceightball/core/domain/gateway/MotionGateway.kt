package com.example.magiceightball.core.domain.gateway

import com.example.magiceightball.core.domain.model.ShakeEvent
import kotlinx.coroutines.flow.Flow

interface MotionGateway {
    val shakeEvents: Flow<ShakeEvent>
}
