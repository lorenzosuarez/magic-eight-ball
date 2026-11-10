package com.lorenzosuarez.magiceightball.phone.di

import android.content.Context
import com.example.magiceightball.core.common.DefaultDispatcherProvider
import com.example.magiceightball.core.common.DispatcherProvider
import com.example.magiceightball.core.data.history.FileAnswerHistoryRepository
import com.example.magiceightball.core.domain.repository.AnswerHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.time.Clock
import javax.inject.Singleton

/** What only the phone app needs: the history, which the watch does not keep, and the time. */
@Module
@InstallIn(SingletonComponent::class)
object PhoneModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    @Singleton
    fun provideAnswerHistoryRepository(
        @ApplicationContext context: Context,
        dispatchers: DispatcherProvider
    ): AnswerHistoryRepository = FileAnswerHistoryRepository(
        // In the app's private files, which the backup rules leave out.
        file = File(context.filesDir, HISTORY_FILE),
        io = dispatchers.io
    )

    private const val HISTORY_FILE = "history.bin"
}
