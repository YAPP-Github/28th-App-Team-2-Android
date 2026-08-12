package com.kikidan.data_remote.di

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.data.datasource.RemoteYearFortuneDataSource
import com.kikidan.data.datasource.RemoteChatDataSource
import com.kikidan.data.datasource.RemoteDayFortuneDataSource
import com.kikidan.data.datasource.RemoteDeviceTokenDataSource
import com.kikidan.data.datasource.RemoteFortuneDataSource
import com.kikidan.data.datasource.RemoteLuckActionDataSource
import com.kikidan.data.datasource.RemoteNotificationDataSource
import com.kikidan.data.datasource.RemotePartnerSajuDataSource
import com.kikidan.data.datasource.RemoteSajuDataSource
import com.kikidan.data.datasource.RemoteUserDataSource
import com.kikidan.data_remote.auth.BearerTokenCacheInvalidator
import com.kikidan.data_remote.datasource.RemoteAuthDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteYearFortuneDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteChatDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteDayFortuneDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteDeviceTokenDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteFortuneDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteLuckActionDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteNotificationDataSourceImpl
import com.kikidan.data_remote.datasource.RemotePartnerSajuDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteSajuDataSourceImpl
import com.kikidan.data_remote.datasource.RemoteUserDataSourceImpl
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

    @Binds
    @Singleton
    abstract fun bindYearFortuneRemoteDataSource(impl: RemoteYearFortuneDataSourceImpl): RemoteYearFortuneDataSource

    @Binds
    @Singleton
    abstract fun bindDayFortuneRemoteDataSource(impl: RemoteDayFortuneDataSourceImpl): RemoteDayFortuneDataSource

    @Binds
    @Singleton
    abstract fun bindFortuneRemoteDataSource(impl: RemoteFortuneDataSourceImpl): RemoteFortuneDataSource

    @Binds
    @Singleton
    abstract fun bindLuckActionRemoteDataSource(impl: RemoteLuckActionDataSourceImpl): RemoteLuckActionDataSource

    @Binds
    @Singleton
    abstract fun bindChatRemoteDataSource(impl: RemoteChatDataSourceImpl): RemoteChatDataSource

    @Binds
    @Singleton
    abstract fun bindUserRemoteDataSource(impl: RemoteUserDataSourceImpl): RemoteUserDataSource

    @Binds
    @Singleton
    abstract fun bindSajuRemoteDataSource(impl: RemoteSajuDataSourceImpl): RemoteSajuDataSource

    @Binds
    @Singleton
    abstract fun bindNotificationRemoteDataSource(impl: RemoteNotificationDataSourceImpl): RemoteNotificationDataSource

    @Binds
    @Singleton
    abstract fun bindPartnerSajuRemoteDataSource(impl: RemotePartnerSajuDataSourceImpl): RemotePartnerSajuDataSource

    @Binds
    @Singleton
    abstract fun bindDeviceTokenDataSource(impl: RemoteDeviceTokenDataSourceImpl): RemoteDeviceTokenDataSource
}
