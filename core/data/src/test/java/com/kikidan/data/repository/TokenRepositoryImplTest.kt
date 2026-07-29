package com.kikidan.data.repository

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.data.fake.FakeInvalidator
import com.kikidan.data.fake.FakeLocalTokenDataSource
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
    private lateinit var fakeTokenDataSource: FakeLocalTokenDataSource
    private lateinit var fakeInvalidator: FakeInvalidator
    private lateinit var sut: TokenRepositoryImpl

    @Before
    fun setUp() {
        fakeTokenDataSource = FakeLocalTokenDataSource()
        fakeInvalidator = FakeInvalidator()
        sut = TokenRepositoryImpl(fakeTokenDataSource, fakeInvalidator)
    }

    @Test
    fun `DataSource의_getToken이_IOException을_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeTokenDataSource.throwOnGet = IOException("disk error")

            // when
            val result = sut.getToken()

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test
    fun `saveToken을_호출하면_AuthTokenCacheInvalidator의_invalidate가_호출된다`() =
        runTest {
            // given
            val token = AuthToken("access", "refresh")

            // when
            sut.saveToken(token)

            // then
            assertTrue(fakeInvalidator.invalidateCalled)
        }

    @Test
    fun `observeLoginState를_구독하면_토큰_존재_여부에_따라_true_또는_false를_방출한다`() =
        runTest {
            // when & then
            fakeTokenDataSource.emit(AuthToken("a", "r"))
            val hasToken = sut.observeLoginState().first()
            assertTrue(hasToken.getOrThrow())

            fakeTokenDataSource.emit(null)
            val noToken = sut.observeLoginState().first()
            assertFalse(noToken.getOrThrow())
        }

    // DataStore.data는 파일 손상 시 실제로 IOException을 방출한다. observeToken() Flow가 예외를
    // 던져도 구독자에게 예외가 그대로 누수되지 않고 Result.failure로 감싸져야 한다(rules/20-data:
    // 예외 누수는 P1).
    @Test
    fun `observeToken_Flow가_IOException을_throw하면_observeLoginState가_Result_failure를_방출하고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeTokenDataSource.throwOnObserve = IOException("datastore corrupted")

            // when
            val result = sut.observeLoginState().first()

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }
}
