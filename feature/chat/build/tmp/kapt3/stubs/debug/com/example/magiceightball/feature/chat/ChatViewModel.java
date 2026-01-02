package com.example.magiceightball.feature.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cJ\b\u0010\u001d\u001a\u00020\u001aH\u0002J\b\u0010\u001e\u001a\u00020\u001aH\u0002J\b\u0010\u001f\u001a\u00020\u001aH\u0002R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006 "}, d2 = {"Lcom/example/magiceightball/feature/chat/ChatViewModel;", "Landroidx/lifecycle/ViewModel;", "sendMessageUseCase", "Lcom/example/magiceightball/core/domain/usecase/SendMessageUseCase;", "observeShakeUseCase", "Lcom/example/magiceightball/core/domain/usecase/ObserveShakeUseCase;", "queryConfig", "Lcom/example/magiceightball/core/domain/model/QueryConfig;", "(Lcom/example/magiceightball/core/domain/usecase/SendMessageUseCase;Lcom/example/magiceightball/core/domain/usecase/ObserveShakeUseCase;Lcom/example/magiceightball/core/domain/model/QueryConfig;)V", "_effects", "Lkotlinx/coroutines/channels/Channel;", "Lcom/example/magiceightball/feature/chat/ChatEffect;", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/magiceightball/feature/chat/ChatState;", "effects", "Lkotlinx/coroutines/flow/Flow;", "getEffects", "()Lkotlinx/coroutines/flow/Flow;", "shakeCollectionJob", "Lkotlinx/coroutines/Job;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "handleIntent", "", "intent", "Lcom/example/magiceightball/feature/chat/ChatIntent;", "onShakeStarted", "startShakeDetection", "stopShakeDetection", "chat_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChatViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.domain.usecase.SendMessageUseCase sendMessageUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase observeShakeUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.domain.model.QueryConfig queryConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.magiceightball.feature.chat.ChatState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.magiceightball.feature.chat.ChatState> state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.channels.Channel<com.example.magiceightball.feature.chat.ChatEffect> _effects = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<com.example.magiceightball.feature.chat.ChatEffect> effects = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job shakeCollectionJob;
    
    @javax.inject.Inject()
    public ChatViewModel(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.usecase.SendMessageUseCase sendMessageUseCase, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase observeShakeUseCase, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.domain.model.QueryConfig queryConfig) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.magiceightball.feature.chat.ChatState> getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.example.magiceightball.feature.chat.ChatEffect> getEffects() {
        return null;
    }
    
    private final void startShakeDetection() {
    }
    
    private final void stopShakeDetection() {
    }
    
    /**
     * Orchestrates the shake-to-query state machine.
     * Transitions: Idle -> Running -> Completed -> Idle.
     * Gates sensor detection during active processing.
     */
    private final void onShakeStarted() {
    }
    
    public final void handleIntent(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.feature.chat.ChatIntent intent) {
    }
}