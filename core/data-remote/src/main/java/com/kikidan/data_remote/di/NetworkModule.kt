package com.kikidan.data_remote.di

import com.kikidan.data.datasource.AuthRemoteDataSource
import com.kikidan.data.datasource.TokenDataSource
import com.kikidan.data_remote.BuildConfig
import com.kikidan.data_remote.client.TodakunJson
import com.kikidan.data_remote.client.installBearerAuth
import com.kikidan.data_remote.client.installTodakunDefaults
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
    fun provideJson(): Json = TodakunJson

    /**
     * OkHttp 엔진을 Singleton으로 1개만 생성해 두 클라이언트가 공유한다.
     * HttpClient(engine) 형태로 생성하면 클라이언트가 엔진을 소유하지 않아
     * 커넥션 풀이 1개로 유지되고 한쪽 close가 엔진을 닫지 않는다.
     */
    @Provides
    @Singleton
    fun provideHttpClientEngine(): HttpClientEngine = OkHttp.create {}

    /**
     * refresh 전용 plain 클라이언트 — Auth 플러그인 없음.
     * Auth 플러그인 재귀를 구조적으로 차단하고, refresh 호출을 독립 유닛 테스트할 수 있게 한다.
     */
    @Provides
    @Singleton
    @TokenRefreshClient
    fun provideTokenRefreshClient(
        engine: HttpClientEngine,
        json: Json,
    ): HttpClient =
        HttpClient(engine) {
            installTodakunDefaults(json, BuildConfig.BASE_URL)
        }

    /**
     * 앱 전역 인증 클라이언트.
     *
     * ★ 반드시 @Singleton으로 유지해야 한다 ★
     * Ktor의 AuthTokenHolder 내부 Mutex가 동일 인스턴스 안에서 refresh 중복을 막는다.
     * 인스턴스가 요청마다 새로 생성되면 이 보장이 통째로 깨져 동시 401 시
     * refresh가 N회 발사된다.
     */
    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedClient(
        engine: HttpClientEngine,
        json: Json,
        tokenDataSource: TokenDataSource,
        authRemoteDataSource: AuthRemoteDataSource,
    ): HttpClient =
        HttpClient(engine) {
            installTodakunDefaults(json, BuildConfig.BASE_URL)
            installBearerAuth(tokenDataSource, authRemoteDataSource)
        }
}
