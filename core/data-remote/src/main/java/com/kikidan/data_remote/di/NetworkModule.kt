package com.kikidan.data_remote.di

import com.kikidan.data.datasource.AuthRemoteDataSource
import com.kikidan.data.datasource.TokenDataSource
import com.kikidan.data_remote.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthenticatedClient(
        engine: HttpClientEngine,
        tokenDataSource: TokenDataSource,
        authRemoteDataSource: AuthRemoteDataSource,
    ): HttpClient =
        HttpClient(OkHttp) {
            installTodakunDefaults(TodakunJson, BuildConfig.BASE_URL)
            installBearerAuth(tokenDataSource, authRemoteDataSource)
        }
}
