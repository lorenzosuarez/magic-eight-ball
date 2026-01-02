package com.example.magiceightball.core.data.repository;

/**
 * Mock implementation of LlmGateway for development and testing.
 * Simulates network latency and returns a deterministic success response.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/example/magiceightball/core/data/repository/MockLlmGateway;", "Lcom/example/magiceightball/core/domain/gateway/LlmGateway;", "config", "Lcom/example/magiceightball/core/domain/model/QueryConfig;", "(Lcom/example/magiceightball/core/domain/model/QueryConfig;)V", "generateMagic8BallMessage", "Lcom/example/magiceightball/core/domain/model/Magic8BallResult;", "request", "Lcom/example/magiceightball/core/domain/model/Magic8BallRequest;", "(Lcom/example/magiceightball/core/domain/model/Magic8BallRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class MockLlmGateway implements com.example.magiceightball.core.domain.gateway.LlmGateway {
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.domain.model.QueryConfig config = null;
    
    @javax.inject.Inject()
    public MockLlmGateway(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.model.QueryConfig config) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object generateMagic8BallMessage(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.model.Magic8BallRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.magiceightball.core.domain.model.Magic8BallResult> $completion) {
        return null;
    }
}