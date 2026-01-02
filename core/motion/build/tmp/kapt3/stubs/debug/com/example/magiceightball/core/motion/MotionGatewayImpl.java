package com.example.magiceightball.core.motion;

/**
 * Implementation of MotionGateway using Android SensorManager.
 * emits accelerometer data streams converted to domain flow.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B!\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u0010H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\t\u001a\u00020\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\u000e\u001a\u0004\b\u000b\u0010\fR\u001a\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u00108VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/example/magiceightball/core/motion/MotionGatewayImpl;", "Lcom/example/magiceightball/core/domain/gateway/MotionGateway;", "context", "Landroid/content/Context;", "strategy", "Lcom/example/magiceightball/core/motion/ShakeDetectionStrategy;", "dispatcherProvider", "Lcom/example/magiceightball/core/common/DispatcherProvider;", "(Landroid/content/Context;Lcom/example/magiceightball/core/motion/ShakeDetectionStrategy;Lcom/example/magiceightball/core/common/DispatcherProvider;)V", "sensorManager", "Landroid/hardware/SensorManager;", "getSensorManager", "()Landroid/hardware/SensorManager;", "sensorManager$delegate", "Lkotlin/Lazy;", "shakeEvents", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/magiceightball/core/domain/model/ShakeEvent;", "getShakeEvents", "()Lkotlinx/coroutines/flow/Flow;", "sensorStream", "Landroid/hardware/SensorEvent;", "motion_debug"})
public final class MotionGatewayImpl implements com.example.magiceightball.core.domain.gateway.MotionGateway {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.motion.ShakeDetectionStrategy strategy = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.common.DispatcherProvider dispatcherProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy sensorManager$delegate = null;
    
    @javax.inject.Inject()
    public MotionGatewayImpl(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.motion.ShakeDetectionStrategy strategy, @org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.common.DispatcherProvider dispatcherProvider) {
        super();
    }
    
    private final android.hardware.SensorManager getSensorManager() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.example.magiceightball.core.domain.model.ShakeEvent> getShakeEvents() {
        return null;
    }
    
    private final kotlinx.coroutines.flow.Flow<android.hardware.SensorEvent> sensorStream() {
        return null;
    }
}