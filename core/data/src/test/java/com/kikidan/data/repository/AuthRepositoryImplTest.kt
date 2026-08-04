package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteAuthDataSource
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OAuthProviderType
import com.kikidan.domain.model.auth.OAuthToken
import com.kikidan.domain.model.auth.OnboardingToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthRepositoryImplTest {
    private lateinit var fakeRemoteAuthDataSource: FakeRemoteAuthDataSource
    private lateinit var sut: AuthRepositoryImpl

    private val credential = OAuthCredential(OAuthProviderType.KAKAO, OAuthToken("token"))

    @Before
    fun setUp() {
        fakeRemoteAuthDataSource = FakeRemoteAuthDataSource()
        sut = AuthRepositoryImpl(fakeRemoteAuthDataSource)
    }

    @Test
    fun `login이_성공하면_DataSource의_LoginResult가_그대로_Result_success로_반환된다`() =
        runTest {
            // given
            val expected = LoginResult(AuthToken("a-1", "r-1"), OnboardingToken("onboarding-1"), newMember = true)
            fakeRemoteAuthDataSource.loginResult = expected

            // when
            val result = sut.login(credential)

            // then
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `DataSource의_login이_예외를_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteAuthDataSource.throwOnLogin = IOException("network error")

            // when
            val result = sut.login(credential)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `DataSource의_login이_CancellationException을_throw하면_Result로_감싸지지_않고_그대로_전파된다`() =
        runTest {
            // given
            fakeRemoteAuthDataSource.throwOnLogin = CancellationException("cancelled")

            // when
            sut.login(credential)
        }

    @Test
    fun `refresh가_성공하면_DataSource의_AuthToken이_그대로_Result_success로_반환된다`() =
        runTest {
            // given
            val expected = AuthToken("new-access", "new-refresh")
            fakeRemoteAuthDataSource.refreshResult = expected

            // when
            val result = sut.refresh("old-refresh")

            // then
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `DataSource의_refresh가_예외를_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteAuthDataSource.throwOnRefresh = IOException("network error")

            // when
            val result = sut.refresh("old-refresh")

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }
}
