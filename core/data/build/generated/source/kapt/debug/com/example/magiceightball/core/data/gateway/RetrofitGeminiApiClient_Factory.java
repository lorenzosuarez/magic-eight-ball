package com.example.magiceightball.core.data.gateway;

import com.example.magiceightball.core.network.api.GeminiService;
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
public final class RetrofitGeminiApiClient_Factory implements Factory<RetrofitGeminiApiClient> {
  private final Provider<GeminiService> serviceProvider;

  public RetrofitGeminiApiClient_Factory(Provider<GeminiService> serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public RetrofitGeminiApiClient get() {
    return newInstance(serviceProvider.get());
  }

  public static RetrofitGeminiApiClient_Factory create(Provider<GeminiService> serviceProvider) {
    return new RetrofitGeminiApiClient_Factory(serviceProvider);
  }

  public static RetrofitGeminiApiClient newInstance(GeminiService service) {
    return new RetrofitGeminiApiClient(service);
  }
}
