package com.example.magiceightball.core.network.di

import com.example.magiceightball.core.domain.gateway.LlmGateway
import com.example.magiceightball.core.domain.policy.PromptPolicy
import com.example.magiceightball.core.domain.policy.DefaultPromptPolicy
import com.example.magiceightball.core.network.api.GeminiService
import com.example.magiceightball.core.network.BuildConfig
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {



    companion object {
        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS) 
            .readTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
        }



        @Provides
        @Singleton
        fun provideGeminiService(retrofit: Retrofit): GeminiService {
            return retrofit.create(GeminiService::class.java)
        }

        @Provides
        @Singleton
        @Named("GeminiApiKey")
        fun provideApiKey(): String {
            return BuildConfig.GEMINI_API_KEY
        }
    }
}
