package com.example.magiceightball.core.motion;

import android.content.Context;
import com.example.magiceightball.core.common.DispatcherProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MotionGatewayImpl_Factory implements Factory<MotionGatewayImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<ShakeDetectionStrategy> strategyProvider;

  private final Provider<DispatcherProvider> dispatcherProvider;

  public MotionGatewayImpl_Factory(Provider<Context> contextProvider,
      Provider<ShakeDetectionStrategy> strategyProvider,
      Provider<DispatcherProvider> dispatcherProvider) {
    this.contextProvider = contextProvider;
    this.strategyProvider = strategyProvider;
    this.dispatcherProvider = dispatcherProvider;
  }

  @Override
  public MotionGatewayImpl get() {
    return newInstance(contextProvider.get(), strategyProvider.get(), dispatcherProvider.get());
  }

  public static MotionGatewayImpl_Factory create(Provider<Context> contextProvider,
      Provider<ShakeDetectionStrategy> strategyProvider,
      Provider<DispatcherProvider> dispatcherProvider) {
    return new MotionGatewayImpl_Factory(contextProvider, strategyProvider, dispatcherProvider);
  }

  public static MotionGatewayImpl newInstance(Context context, ShakeDetectionStrategy strategy,
      DispatcherProvider dispatcherProvider) {
    return new MotionGatewayImpl(context, strategy, dispatcherProvider);
  }
}
