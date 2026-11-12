package com.example.magiceightball.di

import com.example.magiceightball.FirebaseAnswerReports
import com.example.magiceightball.core.common.DefaultDispatcherProvider
import com.example.magiceightball.core.common.DispatcherProvider
import com.example.magiceightball.core.domain.gateway.AnswerReports
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }

    @Provides
    @Singleton
    fun provideAnswerReports(impl: FirebaseAnswerReports): AnswerReports = impl
}
