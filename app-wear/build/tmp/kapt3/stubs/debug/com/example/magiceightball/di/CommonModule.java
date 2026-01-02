package com.example.magiceightball.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007\u00a8\u0006\u0005"}, d2 = {"Lcom/example/magiceightball/di/CommonModule;", "", "()V", "provideDispatcherProvider", "Lcom/example/magiceightball/core/common/DispatcherProvider;", "app-wear_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class CommonModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.magiceightball.di.CommonModule INSTANCE = null;
    
    private CommonModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.example.magiceightball.core.common.DispatcherProvider provideDispatcherProvider() {
        return null;
    }
}