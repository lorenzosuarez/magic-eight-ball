package com.example.magiceightball.feature.chat;

/**
 * Represents the UI state of the chat screen.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B1\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u0015\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u0016\u001a\u0004\u0018\u00010\bH\u00c6\u0003J:\u0010\u0017\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\bH\u00c6\u0001\u00a2\u0006\u0002\u0010\u0018J\u0013\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001c\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u001d\u001a\u00020\bH\u00d6\u0001R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u0010\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001e"}, d2 = {"Lcom/example/magiceightball/feature/chat/ChatState;", "", "machineState", "Lcom/example/magiceightball/feature/chat/ChatStateMachine;", "shakeStatusRes", "", "titleRes", "error", "", "(Lcom/example/magiceightball/feature/chat/ChatStateMachine;Ljava/lang/Integer;ILjava/lang/String;)V", "getError", "()Ljava/lang/String;", "getMachineState", "()Lcom/example/magiceightball/feature/chat/ChatStateMachine;", "getShakeStatusRes", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getTitleRes", "()I", "component1", "component2", "component3", "component4", "copy", "(Lcom/example/magiceightball/feature/chat/ChatStateMachine;Ljava/lang/Integer;ILjava/lang/String;)Lcom/example/magiceightball/feature/chat/ChatState;", "equals", "", "other", "hashCode", "toString", "chat_debug"})
public final class ChatState {
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.feature.chat.ChatStateMachine machineState = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer shakeStatusRes = null;
    private final int titleRes = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    
    public ChatState(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.feature.chat.ChatStateMachine machineState, @org.jetbrains.annotations.Nullable()
    java.lang.Integer shakeStatusRes, int titleRes, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.magiceightball.feature.chat.ChatStateMachine getMachineState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getShakeStatusRes() {
        return null;
    }
    
    public final int getTitleRes() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
    
    public ChatState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.magiceightball.feature.chat.ChatStateMachine component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component2() {
        return null;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.magiceightball.feature.chat.ChatState copy(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.feature.chat.ChatStateMachine machineState, @org.jetbrains.annotations.Nullable()
    java.lang.Integer shakeStatusRes, int titleRes, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}