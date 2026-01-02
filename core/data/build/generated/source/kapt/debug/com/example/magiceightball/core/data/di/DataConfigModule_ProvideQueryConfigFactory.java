package com.example.magiceightball.core.data.di;

import com.example.magiceightball.core.domain.model.QueryConfig;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataConfigModule_ProvideQueryConfigFactory implements Factory<QueryConfig> {
  @Override
  public QueryConfig get() {
    return provideQueryConfig();
  }

  public static DataConfigModule_ProvideQueryConfigFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static QueryConfig provideQueryConfig() {
    return Preconditions.checkNotNullFromProvides(DataConfigModule.INSTANCE.provideQueryConfig());
  }

  private static final class InstanceHolder {
    private static final DataConfigModule_ProvideQueryConfigFactory INSTANCE = new DataConfigModule_ProvideQueryConfigFactory();
  }
}
