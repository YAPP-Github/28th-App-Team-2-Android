package com.kikidan.data.di

import com.kikidan.data.repository.AuthRepositoryImpl
import com.kikidan.data.repository.FortuneRepositoryImpl
import com.kikidan.data.repository.LuckActionRepositoryImpl
import com.kikidan.data.repository.TokenRepositoryImpl
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.FortuneRepository
import com.kikidan.domain.repository.LuckActionRepository
import com.kikidan.domain.repository.TokenRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLuckActionRepository(impl: LuckActionRepositoryImpl): LuckActionRepository

    @Binds
    @Singleton
    abstract fun bindFortuneRepository(impl: FortuneRepositoryImpl): FortuneRepository
}
