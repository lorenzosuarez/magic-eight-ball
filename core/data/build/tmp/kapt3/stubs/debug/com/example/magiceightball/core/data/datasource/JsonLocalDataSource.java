package com.example.magiceightball.core.data.datasource;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\n\u0010\t\u001a\u0004\u0018\u00010\bH\u0002J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bH\u0016R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/example/magiceightball/core/data/datasource/JsonLocalDataSource;", "Lcom/example/magiceightball/core/data/datasource/LocalDataSource;", "context", "Landroid/content/Context;", "json", "Lkotlinx/serialization/json/Json;", "(Landroid/content/Context;Lkotlinx/serialization/json/Json;)V", "cachedData", "Lcom/example/magiceightball/core/data/model/Magic8BallDataDto;", "getOrLoadData", "getRandomMessage", "", "languageCode", "data_debug"})
public final class JsonLocalDataSource implements com.example.magiceightball.core.data.datasource.LocalDataSource {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    @org.jetbrains.annotations.Nullable()
    private com.example.magiceightball.core.data.model.Magic8BallDataDto cachedData;
    
    @javax.inject.Inject()
    public JsonLocalDataSource(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlinx.serialization.json.Json json) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getRandomMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String languageCode) {
        return null;
    }
    
    private final com.example.magiceightball.core.data.model.Magic8BallDataDto getOrLoadData() {
        return null;
    }
}