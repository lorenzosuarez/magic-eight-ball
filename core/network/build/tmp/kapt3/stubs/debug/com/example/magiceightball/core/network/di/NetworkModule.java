package com.example.magiceightball.core.network.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\b\'\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0004"}, d2 = {"Lcom/example/magiceightball/core/network/di/NetworkModule;", "", "()V", "Companion", "network_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class NetworkModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.magiceightball.core.network.di.NetworkModule.Companion Companion = null;
    
    public NetworkModule() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007J\b\u0010\t\u001a\u00020\nH\u0007J\b\u0010\u000b\u001a\u00020\fH\u0007J\u0018\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\nH\u0007\u00a8\u0006\u0010"}, d2 = {"Lcom/example/magiceightball/core/network/di/NetworkModule$Companion;", "", "()V", "provideApiKey", "", "provideGeminiService", "Lcom/example/magiceightball/core/network/api/GeminiService;", "retrofit", "Lretrofit2/Retrofit;", "provideJson", "Lkotlinx/serialization/json/Json;", "provideOkHttpClient", "Lokhttp3/OkHttpClient;", "provideRetrofit", "okHttpClient", "json", "network_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.serialization.json.Json provideJson() {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final okhttp3.OkHttpClient provideOkHttpClient() {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final retrofit2.Retrofit provideRetrofit(@org.jetbrains.annotations.NotNull()
        okhttp3.OkHttpClient okHttpClient, @org.jetbrains.annotations.NotNull()
        kotlinx.serialization.json.Json json) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.example.magiceightball.core.network.api.GeminiService provideGeminiService(@org.jetbrains.annotations.NotNull()
        retrofit2.Retrofit retrofit) {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @javax.inject.Named(value = "GeminiApiKey")
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String provideApiKey() {
            return null;
        }
    }
}