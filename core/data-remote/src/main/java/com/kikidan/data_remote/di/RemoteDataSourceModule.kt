package com.kikidan.data_remote.di

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data.datasource.AuthRemoteDataSource
import com.kikidan.data_remote.auth.BearerTokenCacheInvalidator
import com.kikidan.data_remote.datasource.AuthRemoteDataSourceImpl
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
    abstract fun bindAuthRemoteDataSource(impl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthTokenCacheInvalidator(impl: BearerTokenCacheInvalidator): AuthTokenCacheInvalidator
}
