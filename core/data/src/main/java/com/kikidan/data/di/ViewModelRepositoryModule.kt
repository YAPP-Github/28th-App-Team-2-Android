package com.kikidan.data.di

import com.kikidan.data.repository.FortuneRepositoryImpl
import com.kikidan.domain.repository.FortuneRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelRepositoryModule {
    // FortuneRepositoryImpl은 history 캐시를 들고 있어, ViewModel 생명주기에 맞춰 캐시가 함께 정리되도록
    @Binds
    @ViewModelScoped
    abstract fun bindFortuneRepository(impl: FortuneRepositoryImpl): FortuneRepository
}
