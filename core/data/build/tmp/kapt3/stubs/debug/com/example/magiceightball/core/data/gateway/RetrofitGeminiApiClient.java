package com.example.magiceightball.core.data.gateway;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J&\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000bH\u0096@\u00a2\u0006\u0002\u0010\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/example/magiceightball/core/data/gateway/RetrofitGeminiApiClient;", "Lcom/example/magiceightball/core/data/gateway/GeminiApiClient;", "service", "Lcom/example/magiceightball/core/network/api/GeminiService;", "(Lcom/example/magiceightball/core/network/api/GeminiService;)V", "generateContent", "Lcom/example/magiceightball/core/network/model/GeminiResponse;", "apiKey", "", "modelId", "request", "Lcom/example/magiceightball/core/network/model/GeminiRequest;", "(Ljava/lang/String;Ljava/lang/String;Lcom/example/magiceightball/core/network/model/GeminiRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class RetrofitGeminiApiClient implements com.example.magiceightball.core.data.gateway.GeminiApiClient {
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.network.api.GeminiService service = null;
    
    @javax.inject.Inject()
    public RetrofitGeminiApiClient(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.network.api.GeminiService service) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object generateContent(@org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.network.model.GeminiRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.magiceightball.core.network.model.GeminiResponse> $completion) {
        return null;
    }
}