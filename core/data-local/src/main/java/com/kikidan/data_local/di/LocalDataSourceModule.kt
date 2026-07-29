package com.kikidan.data_local.di

import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.data_local.datasource.LocalTokenDataSourceImpl
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
    abstract fun bindTokenDataSource(impl: LocalTokenDataSourceImpl): LocalTokenDataSource
}
