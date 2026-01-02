package com.example.magiceightball.core.data.datasource;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B)\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0001\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ \u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010\u0015J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\tH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/example/magiceightball/core/data/datasource/GeminiRemoteDataSource;", "", "apiClient", "Lcom/example/magiceightball/core/data/gateway/GeminiApiClient;", "config", "Lcom/example/magiceightball/core/common/GeminiConfig;", "promptPolicy", "Lcom/example/magiceightball/core/domain/policy/PromptPolicy;", "apiKey", "", "(Lcom/example/magiceightball/core/data/gateway/GeminiApiClient;Lcom/example/magiceightball/core/common/GeminiConfig;Lcom/example/magiceightball/core/domain/policy/PromptPolicy;Ljava/lang/String;)V", "executeWithRetry", "Lcom/example/magiceightball/core/domain/model/Magic8BallResult;", "initialRequest", "Lcom/example/magiceightball/core/network/model/GeminiRequest;", "attempt", "", "(Lcom/example/magiceightball/core/network/model/GeminiRequest;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchMagic8BallMessage", "request", "Lcom/example/magiceightball/core/domain/model/Magic8BallRequest;", "(Lcom/example/magiceightball/core/domain/model/Magic8BallRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "validateWordCount", "", "text", "data_debug"})
public final class GeminiRemoteDataSource {
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.data.gateway.GeminiApiClient apiClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.common.GeminiConfig config = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.domain.policy.PromptPolicy promptPolicy = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String apiKey = null;
    
    @javax.inject.Inject()
    public GeminiRemoteDataSource(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.data.gateway.GeminiApiClient apiClient, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.common.GeminiConfig config, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.policy.PromptPolicy promptPolicy, @javax.inject.Named(value = "GeminiApiKey")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object fetchMagic8BallMessage(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.model.Magic8BallRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.magiceightball.core.domain.model.Magic8BallResult> $completion) {
        return null;
    }
    
    private final java.lang.Object executeWithRetry(com.example.magiceightball.core.network.model.GeminiRequest initialRequest, int attempt, kotlin.coroutines.Continuation<? super com.example.magiceightball.core.domain.model.Magic8BallResult> $completion) {
        return null;
    }
    
    private final boolean validateWordCount(java.lang.String text) {
        return false;
    }
}