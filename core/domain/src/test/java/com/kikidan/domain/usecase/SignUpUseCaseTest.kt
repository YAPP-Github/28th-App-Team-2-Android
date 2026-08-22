package com.kikidan.domain.usecase

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.TokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class SignUpUseCaseTest {
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var tokenRepository: FakeTokenRepository
    private lateinit var sut: SignUpUseCase

    private val onboardingToken = OnboardingToken("onboarding-token")
    private val user =
        SignupSubmission(
            name = "토닥이",
            job = Job.STUDENT,
            relationshipStatus = RelationshipStatus.SOLO,
            gender = Gender.FEMALE,
            birth = Birth(DateType.SOLAR, LocalDate.of(1999, 2, 13), BirthTime.JA),
        )

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        tokenRepository = FakeTokenRepository()
        sut = SignUpUseCase(authRepository, tokenRepository)
    }

    @Test
    fun `signup이_성공하면_토큰을_저장하고_AuthToken을_반환한다`() =
        runTest {
            // given
            val expected = AuthToken("access", "refresh")
            authRepository.signupResult = Result.success(expected)

            // when
            val result = sut(user, onboardingToken)

            // then
            assertEquals(expected, result.getOrNull())
            assertEquals(expected, tokenRepository.savedToken)
        }

    @Test
    fun `signup이_실패하면_토큰을_저장하지_않고_실패를_그대로_반환한다`() =
        runTest {
            // given
            val error = IOException("network error")
            authRepository.signupResult = Result.failure(error)

            // when
            val result = sut(user, onboardingToken)

            // then
            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
            assertFalse(tokenRepository.saveCalled)
        }

    @Test
    fun `토큰_저장이_실패하면_실패를_반환한다`() =
        runTest {
            // given
            authRepository.signupResult = Result.success(AuthToken("access", "refresh"))
            val saveError = IOException("disk error")
            tokenRepository.saveResult = Result.failure(saveError)

            // when
            val result = sut(user, onboardingToken)

            // then
            assertTrue(result.isFailure)
            assertEquals(saveError, result.exceptionOrNull())
        }

    private class FakeAuthRepository : AuthRepository {
        var signupResult: Result<AuthToken> = Result.success(AuthToken("access", "refresh"))

        override suspend fun login(credential: OAuthCredential): Result<LoginResult> = error("not used")

        override suspend fun signup(
            signupSubmission: SignupSubmission,
            onboardingToken: OnboardingToken,
        ): Result<AuthToken> = signupResult

        override suspend fun refresh(refreshToken: String): Result<AuthToken> = error("not used")

        override suspend fun logout(): Result<Unit> = error("not used")
    }

    private class FakeTokenRepository : TokenRepository {
        var saveResult: Result<Unit> = Result.success(Unit)
        var saveCalled: Boolean = false
        var savedToken: AuthToken? = null

        override fun observeLoginState(): Flow<Result<Boolean>> = flowOf()

        override suspend fun getToken(): Result<AuthToken?> = error("not used")

        override suspend fun saveToken(token: AuthToken): Result<Unit> {
            saveCalled = true
            savedToken = token
            return saveResult
        }

        override suspend fun clearToken(): Result<Unit> = error("not used")
    }
}
