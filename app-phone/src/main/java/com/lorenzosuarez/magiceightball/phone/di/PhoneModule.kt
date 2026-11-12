package com.lorenzosuarez.magiceightball.phone.di

import android.content.Context
import com.example.magiceightball.core.common.DefaultDispatcherProvider
import com.example.magiceightball.core.common.DispatcherProvider
import com.example.magiceightball.core.data.history.FileAnswerHistoryRepository
import com.example.magiceightball.core.domain.repository.AnswerHistoryRepository
import com.lorenzosuarez.magiceightball.phone.ads.AdMobAdsController
import com.lorenzosuarez.magiceightball.phone.ads.AdsController
import com.lorenzosuarez.magiceightball.phone.config.AppConfigSource
import com.lorenzosuarez.magiceightball.phone.config.RemoteAppConfigSource
import com.lorenzosuarez.magiceightball.phone.usage.FirebaseUsageTracker
import com.example.magiceightball.core.domain.gateway.AnswerReports
import com.example.magiceightball.core.domain.gateway.UsageTracker
import com.lorenzosuarez.magiceightball.phone.usage.FirebaseAnswerReports
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.time.Clock
import javax.inject.Singleton

/** What only the phone app needs: the history and the ads, which the watch has none of, and the time. */
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
    fun provideAdsController(impl: AdMobAdsController): AdsController = impl

    @Provides
    @Singleton
    fun provideAppConfigSource(impl: RemoteAppConfigSource): AppConfigSource = impl

    @Provides
    @Singleton
    fun provideUsageTracker(impl: FirebaseUsageTracker): UsageTracker = impl

    @Provides
    @Singleton
    fun provideAnswerReports(impl: FirebaseAnswerReports): AnswerReports = impl

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
