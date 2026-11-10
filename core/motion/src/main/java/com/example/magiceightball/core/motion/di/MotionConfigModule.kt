package com.example.magiceightball.core.motion.di

import com.example.magiceightball.core.motion.ShakeConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MotionConfigModule {

    @Provides
    @Singleton
    fun provideShakeConfig(): ShakeConfig = ShakeConfig()
}
