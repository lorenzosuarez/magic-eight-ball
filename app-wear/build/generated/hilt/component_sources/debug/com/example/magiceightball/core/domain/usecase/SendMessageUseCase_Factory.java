package com.example.magiceightball.core.domain.usecase;

import com.example.magiceightball.core.domain.gateway.LlmGateway;
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
public final class SendMessageUseCase_Factory implements Factory<SendMessageUseCase> {
  private final Provider<LlmGateway> llmGatewayProvider;

  public SendMessageUseCase_Factory(Provider<LlmGateway> llmGatewayProvider) {
    this.llmGatewayProvider = llmGatewayProvider;
  }

  @Override
  public SendMessageUseCase get() {
    return newInstance(llmGatewayProvider.get());
  }

  public static SendMessageUseCase_Factory create(Provider<LlmGateway> llmGatewayProvider) {
    return new SendMessageUseCase_Factory(llmGatewayProvider);
  }

  public static SendMessageUseCase newInstance(LlmGateway llmGateway) {
    return new SendMessageUseCase(llmGateway);
  }
}
