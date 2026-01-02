package com.example.magiceightball.core.domain.usecase

import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.domain.model.ShakeEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveShakeUseCase @Inject constructor(
    private val motionGateway: MotionGateway
) {
    operator fun invoke(): Flow<ShakeEvent> = motionGateway.shakeEvents
}
