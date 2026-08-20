package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteAuthDataSource
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OAuthProviderType
import com.kikidan.domain.model.auth.OAuthToken
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class AuthRepositoryImplTest {
    private lateinit var fakeRemoteAuthDataSource: FakeRemoteAuthDataSource
    private lateinit var sut: AuthRepositoryImpl

    private val credential = OAuthCredential(OAuthProviderType.KAKAO, OAuthToken("token"))
    private val user =
        SignupSubmission(
            name = "토닥이",
            job = Job.STUDENT,
            relationshipStatus = RelationshipStatus.SOLO,
            gender = Gender.FEMALE,
            birth = Birth(DateType.SOLAR, LocalDate.of(1999, 2, 13), BirthTime.JA),
        )
    private val onboardingToken = OnboardingToken("onboarding-1")

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

    @Test
    fun `signup이_성공하면_DataSource의_AuthToken이_그대로_Result_success로_반환된다`() =
        runTest {
            // given
            val expected = AuthToken("a-2", "r-2")
            fakeRemoteAuthDataSource.signupResult = expected

            // when
            val result = sut.signup(user, onboardingToken)

            // then
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `DataSource의_signup이_예외를_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteAuthDataSource.throwOnSignup = IOException("network error")

            // when
            val result = sut.signup(user, onboardingToken)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `DataSource의_signup이_CancellationException을_throw하면_Result로_감싸지지_않고_그대로_전파된다`() =
        runTest {
            // given
            fakeRemoteAuthDataSource.throwOnSignup = CancellationException("cancelled")

            // when
            sut.signup(user, onboardingToken)
        }

    @Test
    fun `logout이_성공하면_DataSource의_postLogout이_호출되고_Result_success가_반환된다`() =
        runTest {
            // when
            val result = sut.logout()

            // then
            assertTrue(fakeRemoteAuthDataSource.logoutCalled)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `DataSource의_logout이_예외를_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteAuthDataSource.throwOnLogout = IOException("network error")

            // when
            val result = sut.logout()

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `DataSource의_logout이_CancellationException을_throw하면_Result로_감싸지지_않고_그대로_전파된다`() =
        runTest {
            // given
            fakeRemoteAuthDataSource.throwOnLogout = CancellationException("cancelled")

            // when
            sut.logout()
        }
}
