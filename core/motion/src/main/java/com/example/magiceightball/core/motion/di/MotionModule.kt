package com.example.magiceightball.core.motion.di

import com.example.magiceightball.core.domain.gateway.MotionGateway
import com.example.magiceightball.core.motion.MotionGatewayImpl
import com.example.magiceightball.core.motion.ShakeDetectionStrategy
import com.example.magiceightball.core.motion.SimpleThresholdShakeStrategy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MotionModule {

    @Binds
    @Singleton
    abstract fun bindMotionGateway(impl: MotionGatewayImpl): MotionGateway

    @Binds
    @Singleton
    abstract fun bindShakeStrategy(impl: SimpleThresholdShakeStrategy): ShakeDetectionStrategy
}
