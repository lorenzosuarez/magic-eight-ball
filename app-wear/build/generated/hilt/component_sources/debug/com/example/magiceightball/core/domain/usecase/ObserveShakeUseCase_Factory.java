package com.example.magiceightball.core.domain.usecase;

import com.example.magiceightball.core.domain.gateway.MotionGateway;
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
public final class ObserveShakeUseCase_Factory implements Factory<ObserveShakeUseCase> {
  private final Provider<MotionGateway> motionGatewayProvider;

  public ObserveShakeUseCase_Factory(Provider<MotionGateway> motionGatewayProvider) {
    this.motionGatewayProvider = motionGatewayProvider;
  }

  @Override
  public ObserveShakeUseCase get() {
    return newInstance(motionGatewayProvider.get());
  }

  public static ObserveShakeUseCase_Factory create(Provider<MotionGateway> motionGatewayProvider) {
    return new ObserveShakeUseCase_Factory(motionGatewayProvider);
  }

  public static ObserveShakeUseCase newInstance(MotionGateway motionGateway) {
    return new ObserveShakeUseCase(motionGateway);
  }
}
