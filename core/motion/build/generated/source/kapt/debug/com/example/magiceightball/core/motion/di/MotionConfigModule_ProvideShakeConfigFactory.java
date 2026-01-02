package com.example.magiceightball.core.motion.di;

import com.example.magiceightball.core.motion.ShakeConfig;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class MotionConfigModule_ProvideShakeConfigFactory implements Factory<ShakeConfig> {
  @Override
  public ShakeConfig get() {
    return provideShakeConfig();
  }

  public static MotionConfigModule_ProvideShakeConfigFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ShakeConfig provideShakeConfig() {
    return Preconditions.checkNotNullFromProvides(MotionConfigModule.INSTANCE.provideShakeConfig());
  }

  private static final class InstanceHolder {
    private static final MotionConfigModule_ProvideShakeConfigFactory INSTANCE = new MotionConfigModule_ProvideShakeConfigFactory();
  }
}
