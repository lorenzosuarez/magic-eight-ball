package com.example.magiceightball.core.common;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GeminiConfig_Factory implements Factory<GeminiConfig> {
  @Override
  public GeminiConfig get() {
    return newInstance();
  }

  public static GeminiConfig_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GeminiConfig newInstance() {
    return new GeminiConfig();
  }

  private static final class InstanceHolder {
    private static final GeminiConfig_Factory INSTANCE = new GeminiConfig_Factory();
  }
}
