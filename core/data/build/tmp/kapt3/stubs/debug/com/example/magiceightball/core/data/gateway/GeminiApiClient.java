package com.example.magiceightball.core.data.gateway;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J&\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010\t\u00a8\u0006\n"}, d2 = {"Lcom/example/magiceightball/core/data/gateway/GeminiApiClient;", "", "generateContent", "Lcom/example/magiceightball/core/network/model/GeminiResponse;", "apiKey", "", "modelId", "request", "Lcom/example/magiceightball/core/network/model/GeminiRequest;", "(Ljava/lang/String;Ljava/lang/String;Lcom/example/magiceightball/core/network/model/GeminiRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public abstract interface GeminiApiClient {
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object generateContent(@org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.network.model.GeminiRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.magiceightball.core.network.model.GeminiResponse> $completion);
}