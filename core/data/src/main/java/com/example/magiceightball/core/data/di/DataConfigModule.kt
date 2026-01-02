package com.example.magiceightball.core.data.di

import com.example.magiceightball.core.data.datasource.JsonLocalDataSource
import com.example.magiceightball.core.data.datasource.LocalDataSource
import com.example.magiceightball.core.data.gateway.GeminiApiClient
import com.example.magiceightball.core.data.gateway.RetrofitGeminiApiClient
import com.example.magiceightball.core.data.repository.Magic8BallRepositoryImpl
import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.model.Magic8BallResult
import com.example.magiceightball.core.domain.model.QueryConfig
import com.example.magiceightball.core.domain.policy.DefaultPromptPolicy
import com.example.magiceightball.core.domain.policy.PromptPolicy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataConfigModule {

    @Provides
    @Singleton
    fun provideQueryConfig(): QueryConfig {
        return QueryConfig()
    }

    // Overrides NetworkModule's binding if applied later, or we need to replace it.
    // Ideally we should disable NetworkModule or create a separate flavor.
    // For this task, we will bind here and assume we replace usage or update NetworkModule.
    // Let's create a specific module for MockGateway binding to replace the real one.
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
    abstract fun bindPromptPolicy(impl: DefaultPromptPolicy): PromptPolicy
}
