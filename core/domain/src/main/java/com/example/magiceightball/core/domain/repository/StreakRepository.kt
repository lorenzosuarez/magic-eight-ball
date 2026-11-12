package com.example.magiceightball.core.domain.repository

import com.example.magiceightball.core.domain.model.Streak
import kotlinx.coroutines.flow.Flow

/** Keeps the [Streak] on the device. It is never sent anywhere. */
interface StreakRepository {
    val streak: Flow<Streak>

    /** The ball has answered on [epochDay]: see [Streak.after]. */
    suspend fun recordAnswer(epochDay: Long)
}
