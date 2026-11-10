package com.example.magiceightball.core.data.di

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.magiceightball.core.data.config.GeminiSettingsSource
import com.example.magiceightball.core.data.config.RemoteConfigGeminiSettingsSource
import com.example.magiceightball.core.data.datasource.JsonLocalDataSource
import com.example.magiceightball.core.data.datasource.LocalDataSource
import com.example.magiceightball.core.data.gateway.GeminiApiClient
import com.example.magiceightball.core.data.gateway.RetrofitGeminiApiClient
import com.example.magiceightball.core.data.repository.Magic8BallRepositoryImpl
import com.example.magiceightball.core.data.settings.DataStoreUserSettingsRepository
import com.example.magiceightball.core.data.speech.AndroidSpeechListener
import com.example.magiceightball.core.domain.gateway.AiAnswersAvailability
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.gateway.SpeechListener
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.policy.DefaultPromptPolicy
import com.example.magiceightball.core.domain.policy.PromptPolicy
import com.example.magiceightball.core.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataConfigModule {

    @Provides
    @Singleton
    fun provideQueryConfig(): QueryConfig {
        return QueryConfig()
    }

    @Provides
    @Singleton
    fun provideUserSettingsRepository(
        @ApplicationContext context: Context
    ): UserSettingsRepository = DataStoreUserSettingsRepository(
        dataStore = PreferenceDataStoreFactory.create(
            // A damaged file is replaced by an empty one, which brings the defaults back.
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            produceFile = { context.preferencesDataStoreFile(USER_SETTINGS_FILE) }
        ),
        deviceLanguageCode = { Locale.getDefault().language }
    )

    private const val USER_SETTINGS_FILE = "user_settings"
}

@Module
@InstallIn(SingletonComponent::class)
abstract class GatewayModule {
    @Binds
    @Singleton
    abstract fun bindLlmGateway(impl: Magic8BallRepositoryImpl): LlmGateway

    @Binds
    @Singleton
    abstract fun bindLocalDataSource(impl: JsonLocalDataSource): LocalDataSource

    @Binds
    @Singleton
    abstract fun bindGeminiApiClient(impl: RetrofitGeminiApiClient): GeminiApiClient

    @Binds
    @Singleton
    abstract fun bindGeminiSettingsSource(impl: RemoteConfigGeminiSettingsSource): GeminiSettingsSource

    @Binds
    @Singleton
    abstract fun bindAiAnswersAvailability(impl: RemoteConfigGeminiSettingsSource): AiAnswersAvailability

    @Binds
    @Singleton
    abstract fun bindSpeechListener(impl: AndroidSpeechListener): SpeechListener

    @Binds
    @Singleton
    abstract fun bindPromptPolicy(impl: DefaultPromptPolicy): PromptPolicy
}
