package com.example.magiceightball.core.data.datasource;

import com.example.magiceightball.core.common.GeminiConfig;
import com.example.magiceightball.core.data.gateway.GeminiApiClient;
import com.example.magiceightball.core.domain.policy.PromptPolicy;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("javax.inject.Named")
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
public final class GeminiRemoteDataSource_Factory implements Factory<GeminiRemoteDataSource> {
  private final Provider<GeminiApiClient> apiClientProvider;

  private final Provider<GeminiConfig> configProvider;

  private final Provider<PromptPolicy> promptPolicyProvider;

  private final Provider<String> apiKeyProvider;

  public GeminiRemoteDataSource_Factory(Provider<GeminiApiClient> apiClientProvider,
      Provider<GeminiConfig> configProvider, Provider<PromptPolicy> promptPolicyProvider,
      Provider<String> apiKeyProvider) {
    this.apiClientProvider = apiClientProvider;
    this.configProvider = configProvider;
    this.promptPolicyProvider = promptPolicyProvider;
    this.apiKeyProvider = apiKeyProvider;
  }

  @Override
  public GeminiRemoteDataSource get() {
    return newInstance(apiClientProvider.get(), configProvider.get(), promptPolicyProvider.get(), apiKeyProvider.get());
  }

  public static GeminiRemoteDataSource_Factory create(Provider<GeminiApiClient> apiClientProvider,
      Provider<GeminiConfig> configProvider, Provider<PromptPolicy> promptPolicyProvider,
      Provider<String> apiKeyProvider) {
    return new GeminiRemoteDataSource_Factory(apiClientProvider, configProvider, promptPolicyProvider, apiKeyProvider);
  }

  public static GeminiRemoteDataSource newInstance(GeminiApiClient apiClient, GeminiConfig config,
      PromptPolicy promptPolicy, String apiKey) {
    return new GeminiRemoteDataSource(apiClient, config, promptPolicy, apiKey);
  }
}
