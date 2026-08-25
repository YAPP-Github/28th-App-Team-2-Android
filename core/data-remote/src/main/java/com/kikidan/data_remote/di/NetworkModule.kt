package com.kikidan.data_remote.di

import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.data_remote.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthenticatedClient(
        localTokenDataSource: LocalTokenDataSource,
        remoteAuthDataSource: RemoteAuthDataSource,
    ): HttpClient =
        HttpClient(OkHttp) {
            installTodakunDefaults(TodakunJson, BuildConfig.BASE_URL)
            installBearerAuth(localTokenDataSource, remoteAuthDataSource)
            engine {
                config {
                    connectTimeout(100, TimeUnit.SECONDS)
                    readTimeout(100, TimeUnit.SECONDS)
                    writeTimeout(100, TimeUnit.SECONDS)
                }
            }
        }
}
