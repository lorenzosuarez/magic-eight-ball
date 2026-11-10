package com.example.magiceightball.core.network.di

import com.example.magiceightball.core.network.BuildConfig
import com.example.magiceightball.core.network.api.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            // BASIC logs method, URL and status only: no headers (API key) and no bodies.
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGeminiService(retrofit: Retrofit): GeminiService =
        retrofit.create(GeminiService::class.java)

    /** Optional key from local.properties, used only when Remote Config provides none (local development). */
    @Provides
    @Singleton
    @Named("BuildGeminiApiKey")
    fun provideBuildApiKey(): String = BuildConfig.GEMINI_API_KEY
}
