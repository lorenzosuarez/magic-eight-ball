package com.example.magiceightball.core.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000bJ\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/example/magiceightball/core/data/repository/Magic8BallRepositoryImpl;", "Lcom/example/magiceightball/core/domain/gateway/LlmGateway;", "remoteDataSource", "Lcom/example/magiceightball/core/data/datasource/GeminiRemoteDataSource;", "localDataSource", "Lcom/example/magiceightball/core/data/datasource/LocalDataSource;", "(Lcom/example/magiceightball/core/data/datasource/GeminiRemoteDataSource;Lcom/example/magiceightball/core/data/datasource/LocalDataSource;)V", "generateMagic8BallMessage", "Lcom/example/magiceightball/core/domain/model/Magic8BallResult;", "request", "Lcom/example/magiceightball/core/domain/model/Magic8BallRequest;", "(Lcom/example/magiceightball/core/domain/model/Magic8BallRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "shouldFallback", "", "error", "Lcom/example/magiceightball/core/domain/model/LlmError;", "data_debug"})
public final class Magic8BallRepositoryImpl implements com.example.magiceightball.core.domain.gateway.LlmGateway {
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource remoteDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.data.datasource.LocalDataSource localDataSource = null;
    
    @javax.inject.Inject()
    public Magic8BallRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource remoteDataSource, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.data.datasource.LocalDataSource localDataSource) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object generateMagic8BallMessage(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.model.Magic8BallRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.magiceightball.core.domain.model.Magic8BallResult> $completion) {
        return null;
    }
    
    private final boolean shouldFallback(com.example.magiceightball.core.domain.model.LlmError error) {
        return false;
    }
}