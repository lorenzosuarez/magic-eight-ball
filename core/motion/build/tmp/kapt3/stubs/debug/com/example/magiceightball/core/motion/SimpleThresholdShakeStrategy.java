package com.example.magiceightball.core.motion;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0014\n\u0002\b\u0003\u0018\u0000 \r2\u00020\u0001:\u0002\r\u000eB\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\tH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/example/magiceightball/core/motion/SimpleThresholdShakeStrategy;", "Lcom/example/magiceightball/core/motion/ShakeDetectionStrategy;", "config", "Lcom/example/magiceightball/core/motion/ShakeConfig;", "(Lcom/example/magiceightball/core/motion/ShakeConfig;)V", "startThreshold", "", "stopThreshold", "detect", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/magiceightball/core/domain/model/ShakeEvent;", "sensorData", "", "Companion", "ShakeState", "motion_debug"})
public final class SimpleThresholdShakeStrategy implements com.example.magiceightball.core.motion.ShakeDetectionStrategy {
    @org.jetbrains.annotations.NotNull()
    private final com.example.magiceightball.core.motion.ShakeConfig config = null;
    private static final float STANDARD_GRAVITY = 9.81F;
    private final float startThreshold = 0.0F;
    private final float stopThreshold = 0.0F;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.magiceightball.core.motion.SimpleThresholdShakeStrategy.Companion Companion = null;
    
    @javax.inject.Inject()
    public SimpleThresholdShakeStrategy(@org.jetbrains.annotations.NotNull()
    com.example.magiceightball.core.motion.ShakeConfig config) {
        super();
    }
    
    /**
     * Detects shake events based on accelerometer magnitude thresholds.
     * Uses a simple state machine (IDLE <-> SHAKING) with hysteresis.
     * Ignores the initial state to prevent false triggering on startup.
     */
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.example.magiceightball.core.domain.model.ShakeEvent> detect(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.Flow<float[]> sensorData) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/example/magiceightball/core/motion/SimpleThresholdShakeStrategy$Companion;", "", "()V", "STANDARD_GRAVITY", "", "motion_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/example/magiceightball/core/motion/SimpleThresholdShakeStrategy$ShakeState;", "", "(Ljava/lang/String;I)V", "IDLE", "SHAKING", "motion_debug"})
    static enum ShakeState {
        /*public static final*/ IDLE /* = new IDLE() */,
        /*public static final*/ SHAKING /* = new SHAKING() */;
        
        ShakeState() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.example.magiceightball.core.motion.SimpleThresholdShakeStrategy.ShakeState> getEntries() {
            return null;
        }
    }
}