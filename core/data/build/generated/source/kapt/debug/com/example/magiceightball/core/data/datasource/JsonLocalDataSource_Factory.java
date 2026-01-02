package com.example.magiceightball.core.data.datasource;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.serialization.json.Json;

@ScopeMetadata("javax.inject.Singleton")
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
public final class JsonLocalDataSource_Factory implements Factory<JsonLocalDataSource> {
  private final Provider<Context> contextProvider;

  private final Provider<Json> jsonProvider;

  public JsonLocalDataSource_Factory(Provider<Context> contextProvider,
      Provider<Json> jsonProvider) {
    this.contextProvider = contextProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public JsonLocalDataSource get() {
    return newInstance(contextProvider.get(), jsonProvider.get());
  }

  public static JsonLocalDataSource_Factory create(Provider<Context> contextProvider,
      Provider<Json> jsonProvider) {
    return new JsonLocalDataSource_Factory(contextProvider, jsonProvider);
  }

  public static JsonLocalDataSource newInstance(Context context, Json json) {
    return new JsonLocalDataSource(context, json);
  }
}
