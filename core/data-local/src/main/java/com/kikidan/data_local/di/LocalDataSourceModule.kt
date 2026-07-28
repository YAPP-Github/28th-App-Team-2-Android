package com.kikidan.data_local.di

import com.kikidan.data.datasource.TokenDataSource
import com.kikidan.data_local.datasource.TokenLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindTokenDataSource(impl: TokenLocalDataSource): TokenDataSource
}
