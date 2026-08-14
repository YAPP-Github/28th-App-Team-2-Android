package com.kikidan.data.di

import com.kikidan.data.repository.FakeNotificationRepository
import com.kikidan.data.repository.FakePartnerSajuRepository
import com.kikidan.data.repository.FakeSajuRepository
import com.kikidan.data.repository.FakeUserRepository
import com.kikidan.domain.di.Fake
import com.kikidan.domain.repository.NotificationRepository
import com.kikidan.domain.repository.PartnerSajuRepository
import com.kikidan.domain.repository.SajuRepository
import com.kikidan.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FakeRepositoryModule {
    @Binds
    @Fake
    fun bindFakeUserRepository(fakeUserRepository: FakeUserRepository): UserRepository

    @Binds
    @Fake
    fun bindFakeSajuRepository(fakeSajuRepository: FakeSajuRepository): SajuRepository

    @Binds
    @Fake
    fun bindFakeNotificationRepository(fakeNotificationRepository: FakeNotificationRepository): NotificationRepository

    @Binds
    @Fake
    fun bindFakePartnerSajuRepository(fakePartnerSajuRepository: FakePartnerSajuRepository): PartnerSajuRepository
}
