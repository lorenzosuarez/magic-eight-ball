package com.example.magiceightball.feature.chat;

import com.example.magiceightball.core.domain.model.QueryConfig;
import com.example.magiceightball.core.domain.usecase.ObserveShakeUseCase;
import com.example.magiceightball.core.domain.usecase.SendMessageUseCase;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<SendMessageUseCase> sendMessageUseCaseProvider;

  private final Provider<ObserveShakeUseCase> observeShakeUseCaseProvider;

  private final Provider<QueryConfig> queryConfigProvider;

  public ChatViewModel_Factory(Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<ObserveShakeUseCase> observeShakeUseCaseProvider,
      Provider<QueryConfig> queryConfigProvider) {
    this.sendMessageUseCaseProvider = sendMessageUseCaseProvider;
    this.observeShakeUseCaseProvider = observeShakeUseCaseProvider;
    this.queryConfigProvider = queryConfigProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(sendMessageUseCaseProvider.get(), observeShakeUseCaseProvider.get(), queryConfigProvider.get());
  }

  public static ChatViewModel_Factory create(
      Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<ObserveShakeUseCase> observeShakeUseCaseProvider,
      Provider<QueryConfig> queryConfigProvider) {
    return new ChatViewModel_Factory(sendMessageUseCaseProvider, observeShakeUseCaseProvider, queryConfigProvider);
  }

  public static ChatViewModel newInstance(SendMessageUseCase sendMessageUseCase,
      ObserveShakeUseCase observeShakeUseCase, QueryConfig queryConfig) {
    return new ChatViewModel(sendMessageUseCase, observeShakeUseCase, queryConfig);
  }
}
