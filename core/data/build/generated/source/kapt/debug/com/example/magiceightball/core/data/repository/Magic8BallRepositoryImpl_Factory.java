package com.example.magiceightball.core.data.repository;

import com.example.magiceightball.core.data.datasource.GeminiRemoteDataSource;
import com.example.magiceightball.core.data.datasource.LocalDataSource;
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
public final class Magic8BallRepositoryImpl_Factory implements Factory<Magic8BallRepositoryImpl> {
  private final Provider<GeminiRemoteDataSource> remoteDataSourceProvider;

  private final Provider<LocalDataSource> localDataSourceProvider;

  public Magic8BallRepositoryImpl_Factory(Provider<GeminiRemoteDataSource> remoteDataSourceProvider,
      Provider<LocalDataSource> localDataSourceProvider) {
    this.remoteDataSourceProvider = remoteDataSourceProvider;
    this.localDataSourceProvider = localDataSourceProvider;
  }

  @Override
  public Magic8BallRepositoryImpl get() {
    return newInstance(remoteDataSourceProvider.get(), localDataSourceProvider.get());
  }

  public static Magic8BallRepositoryImpl_Factory create(
      Provider<GeminiRemoteDataSource> remoteDataSourceProvider,
      Provider<LocalDataSource> localDataSourceProvider) {
    return new Magic8BallRepositoryImpl_Factory(remoteDataSourceProvider, localDataSourceProvider);
  }

  public static Magic8BallRepositoryImpl newInstance(GeminiRemoteDataSource remoteDataSource,
      LocalDataSource localDataSource) {
    return new Magic8BallRepositoryImpl(remoteDataSource, localDataSource);
  }
}
