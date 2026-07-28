package com.kikidan.data.repository

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data.datasource.TokenDataSource
import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class TokenRepositoryImplTest {
    private lateinit var fakeTokenDataSource: FakeTokenDataSource
    private lateinit var fakeInvalidator: FakeInvalidator
    private lateinit var sut: TokenRepositoryImpl

    @Before
    fun setUp() {
        fakeTokenDataSource = FakeTokenDataSource()
        fakeInvalidator = FakeInvalidator()
        sut = TokenRepositoryImpl(fakeTokenDataSource, fakeInvalidator)
    }

    /** T15: DataSource가 IOException throw → Result.isFailure, 예외 누수 없음 (rules/20-data) */
    @Test
    fun `T15 - DataSource IOException 시 Result failure, 예외 누수 없음`() =
        runTest {
            fakeTokenDataSource.throwOnGet = IOException("disk error")

            val result = sut.getToken()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    /** T16: saveToken 성공 → AuthTokenCacheInvalidator.invalidate() 호출됨 */
    @Test
    fun `T16 - saveToken 성공 시 invalidate 호출`() =
        runTest {
            val token = AuthToken("access", "refresh")

            sut.saveToken(token)

            assertTrue(fakeInvalidator.invalidateCalled)
        }

    /** T17: observeLoginState — 토큰 있음 → true, clearToken 후 → false */
    @Test
    fun `T17 - observeLoginState 토큰 유무에 따라 true·false`() =
        runTest {
            fakeTokenDataSource.emit(AuthToken("a", "r"))
            val hasToken = sut.observeLoginState().first()
            assertTrue(hasToken.getOrThrow())

            fakeTokenDataSource.emit(null)
            val noToken = sut.observeLoginState().first()
            assertFalse(noToken.getOrThrow())
        }

    /**
     * T18: observeToken() Flow가 예외를 던져도 구독자에게 예외가 누수되지 않고
     * Result.failure로 방출된다 (rules/20-data: 예외 누수는 P1).
     * DataStore.data 는 파일 손상 시 실제로 IOException 을 방출한다.
     */
    @Test
    fun `T18 - observeToken Flow 예외 시 Result failure 방출, 예외 누수 없음`() =
        runTest {
            fakeTokenDataSource.throwOnObserve = IOException("datastore corrupted")

            val result = sut.observeLoginState().first()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    // ── 테스트 전용 더블 ──────────────────────────────────────────────────────

    private class FakeTokenDataSource : TokenDataSource {
        private val tokenFlow = MutableStateFlow<AuthToken?>(null)
        var throwOnGet: Throwable? = null
        var throwOnObserve: Throwable? = null

        fun emit(token: AuthToken?) {
            tokenFlow.value = token
        }

        override fun observeToken(): Flow<AuthToken?> =
            throwOnObserve?.let { error -> flow { throw error } } ?: tokenFlow

        override suspend fun getToken(): AuthToken? {
            throwOnGet?.let { throw it }
            return tokenFlow.value
        }

        override suspend fun saveToken(token: AuthToken) {
            tokenFlow.value = token
        }

        override suspend fun clearToken() {
            tokenFlow.value = null
        }
    }

    private class FakeInvalidator : AuthTokenCacheInvalidator {
        var invalidateCalled = false

        override fun invalidate() {
            invalidateCalled = true
        }
    }
}
