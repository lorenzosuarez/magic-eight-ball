package com.example.magiceightball.core.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J,\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\t\u00a8\u0006\n"}, d2 = {"Lcom/example/magiceightball/core/network/api/GeminiService;", "", "generateContent", "Lcom/example/magiceightball/core/network/model/GeminiResponse;", "modelId", "", "apiKey", "request", "Lcom/example/magiceightball/core/network/model/GeminiRequest;", "(Ljava/lang/String;Ljava/lang/String;Lcom/example/magiceightball/core/network/model/GeminiRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "network_debug"})
public abstract interface GeminiService {
    
    @retrofit2.http.POST(value = "v1beta/models/{modelId}:generateContent")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object generateContent(@retrofit2.http.Path(value = "modelId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @retrofit2.http.Query(value = "key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.network.model.GeminiRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.magiceightball.core.network.model.GeminiResponse> $completion);
}