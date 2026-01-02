package com.example.magiceightball.core.network.di;

import com.example.magiceightball.core.network.api.GeminiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_Companion_ProvideGeminiServiceFactory implements Factory<GeminiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_Companion_ProvideGeminiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public GeminiService get() {
    return provideGeminiService(retrofitProvider.get());
  }

  public static NetworkModule_Companion_ProvideGeminiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_Companion_ProvideGeminiServiceFactory(retrofitProvider);
  }

  public static GeminiService provideGeminiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.Companion.provideGeminiService(retrofit));
  }
}
