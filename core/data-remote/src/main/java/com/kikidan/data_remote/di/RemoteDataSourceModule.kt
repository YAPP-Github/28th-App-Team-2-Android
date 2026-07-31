package com.kikidan.data_remote.di

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.data_remote.auth.BearerTokenCacheInvalidator
import com.kikidan.data_remote.datasource.RemoteAuthDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(impl: RemoteAuthDataSourceImpl): RemoteAuthDataSource

    @Binds
    @Singleton
    abstract fun bindAuthTokenCacheInvalidator(impl: BearerTokenCacheInvalidator): AuthTokenCacheInvalidator
}
