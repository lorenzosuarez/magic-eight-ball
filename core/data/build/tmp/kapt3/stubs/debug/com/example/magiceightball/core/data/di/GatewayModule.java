package com.example.magiceightball.core.data.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\fH\'J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000fH\'\u00a8\u0006\u0010"}, d2 = {"Lcom/example/magiceightball/core/data/di/GatewayModule;", "", "()V", "bindGeminiApiClient", "Lcom/example/magiceightball/core/data/gateway/GeminiApiClient;", "impl", "Lcom/example/magiceightball/core/data/gateway/RetrofitGeminiApiClient;", "bindLlmGateway", "Lcom/example/magiceightball/core/domain/gateway/LlmGateway;", "Lcom/example/magiceightball/core/data/repository/Magic8BallRepositoryImpl;", "bindLocalDataSource", "Lcom/example/magiceightball/core/data/datasource/LocalDataSource;", "Lcom/example/magiceightball/core/data/datasource/JsonLocalDataSource;", "bindPromptPolicy", "Lcom/example/magiceightball/core/domain/policy/PromptPolicy;", "Lcom/example/magiceightball/core/domain/policy/DefaultPromptPolicy;", "data_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class GatewayModule {
    
    public GatewayModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.magiceightball.core.domain.gateway.LlmGateway bindLlmGateway(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.data.repository.Magic8BallRepositoryImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.magiceightball.core.data.datasource.LocalDataSource bindLocalDataSource(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.data.datasource.JsonLocalDataSource impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.magiceightball.core.data.gateway.GeminiApiClient bindGeminiApiClient(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.data.gateway.RetrofitGeminiApiClient impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.magiceightball.core.domain.policy.PromptPolicy bindPromptPolicy(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.policy.DefaultPromptPolicy impl);
}