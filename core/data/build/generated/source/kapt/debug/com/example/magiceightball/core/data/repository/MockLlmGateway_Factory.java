package com.example.magiceightball.core.data.repository;

import com.example.magiceightball.core.domain.model.QueryConfig;
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
public final class MockLlmGateway_Factory implements Factory<MockLlmGateway> {
  private final Provider<QueryConfig> configProvider;

  public MockLlmGateway_Factory(Provider<QueryConfig> configProvider) {
    this.configProvider = configProvider;
  }

  @Override
  public MockLlmGateway get() {
    return newInstance(configProvider.get());
  }

  public static MockLlmGateway_Factory create(Provider<QueryConfig> configProvider) {
    return new MockLlmGateway_Factory(configProvider);
  }

  public static MockLlmGateway newInstance(QueryConfig config) {
    return new MockLlmGateway(config);
  }
}
