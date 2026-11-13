package com.lorenzosuarez.magiceightball.phone.di

import com.lorenzosuarez.magiceightball.phone.account.FirebaseAccount
import com.lorenzosuarez.magiceightball.phone.account.Account
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
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.lorenzosuarez.magiceightball.phone.account.CustomCharacters
import com.lorenzosuarez.magiceightball.phone.account.DataStoreDeviceMarks
import com.lorenzosuarez.magiceightball.phone.account.FirestoreCustomCharacters
import com.lorenzosuarez.magiceightball.phone.account.DeviceMarks
import com.lorenzosuarez.magiceightball.phone.account.FirestoreProfiles
import com.lorenzosuarez.magiceightball.phone.account.Profiles
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
    fun provideAccount(impl: FirebaseAccount): Account = impl

    @Provides
    @Singleton
    fun provideProfiles(impl: FirestoreProfiles): Profiles = impl

    @Provides
    @Singleton
    fun provideCustomCharacters(impl: FirestoreCustomCharacters): CustomCharacters = impl

    @Provides
    @Singleton
    fun provideDeviceMarks(@ApplicationContext context: Context): DeviceMarks = DataStoreDeviceMarks(
        PreferenceDataStoreFactory.create(
            // A damaged file is replaced by an empty one, which holds no marks.
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            produceFile = { context.preferencesDataStoreFile(DEVICE_MARKS_FILE) }
        )
    )

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
    private const val DEVICE_MARKS_FILE = "device_marks"
}
