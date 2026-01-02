package com.example.magiceightball.core.network.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NetworkModule_Companion_ProvideApiKeyFactory implements Factory<String> {
  @Override
  public String get() {
    return provideApiKey();
  }

  public static NetworkModule_Companion_ProvideApiKeyFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static String provideApiKey() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.Companion.provideApiKey());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_Companion_ProvideApiKeyFactory INSTANCE = new NetworkModule_Companion_ProvideApiKeyFactory();
  }
}
