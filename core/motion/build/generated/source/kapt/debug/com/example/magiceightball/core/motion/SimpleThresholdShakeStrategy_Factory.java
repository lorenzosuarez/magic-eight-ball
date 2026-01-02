package com.example.magiceightball.core.motion;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SimpleThresholdShakeStrategy_Factory implements Factory<SimpleThresholdShakeStrategy> {
  private final Provider<ShakeConfig> configProvider;

  public SimpleThresholdShakeStrategy_Factory(Provider<ShakeConfig> configProvider) {
    this.configProvider = configProvider;
  }

  @Override
  public SimpleThresholdShakeStrategy get() {
    return newInstance(configProvider.get());
  }

  public static SimpleThresholdShakeStrategy_Factory create(Provider<ShakeConfig> configProvider) {
    return new SimpleThresholdShakeStrategy_Factory(configProvider);
  }

  public static SimpleThresholdShakeStrategy newInstance(ShakeConfig config) {
    return new SimpleThresholdShakeStrategy(config);
  }
}
