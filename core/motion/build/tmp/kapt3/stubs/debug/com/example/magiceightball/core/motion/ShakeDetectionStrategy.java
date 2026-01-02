package com.example.magiceightball.core.motion;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0014\n\u0000\bf\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00060\u0003H&\u00a8\u0006\u0007"}, d2 = {"Lcom/example/magiceightball/core/motion/ShakeDetectionStrategy;", "", "detect", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/magiceightball/core/domain/model/ShakeEvent;", "sensorData", "", "motion_debug"})
public abstract interface ShakeDetectionStrategy {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.example.magiceightball.core.domain.model.ShakeEvent> detect(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.Flow<float[]> sensorData);
}