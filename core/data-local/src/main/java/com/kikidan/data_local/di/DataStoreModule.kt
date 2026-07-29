package com.kikidan.data_local.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// app/src/main/res/xml/backup_rules.xml 및 data_extraction_rules.xml과 경로가 일치해야 한다
private val Context.authTokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_token")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    @AuthTokenDataStore
    fun provideAuthTokenDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.authTokenDataStore
}
