package com.kikidan.onboarding

import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.domain.model.onboarding.UserName
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.SignUpUseCase
import com.kikidan.onboarding.fake.FakeAuthRepository
import com.kikidan.onboarding.fake.FakeTokenRepository
import com.kikidan.onboarding.model.OnboardingSheet
import com.kikidan.onboarding.model.OnboardingSideEffect
import com.kikidan.onboarding.model.OnboardingState
import com.kikidan.onboarding.model.OnboardingStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var tokenRepository: FakeTokenRepository

    private val onboardingToken = OnboardingToken("onboarding-token")

    private fun viewModel() = OnboardingViewModel(SignUpUseCase(authRepository, tokenRepository))

    @Before
    fun setUp() {
        // viewModelScope가 Dispatchers.Main을 요구하므로 테스트 디스패처로 바꿔둔다.
        Dispatchers.setMain(StandardTestDispatcher())
        authRepository = FakeAuthRepository()
        tokenRepository = FakeTokenRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `특수문자가 든 이름은 에러로 표시되고 다음으로 넘어갈 수 없다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.NAME)
            viewModel().test(this, initialState = initial) {
                containerHost.changeName("토닥이##")
                expectState {
                    copy(username = UserName.Invalid.ContainsSpecialCharacter("토닥이##"))
                }

                containerHost.clickNext()
                expectNoItems()
            }
        }

    @Test
    fun `유효한 이름을 넣으면 다음 스텝으로 넘어간다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.NAME)
            viewModel().test(this, initialState = initial) {
                containerHost.changeName("토닥이")
                expectState { copy(username = UserName.Valid("토닥이")) }

                containerHost.clickNext()
                expectState { copy(step = OnboardingStep.BIRTH_INFO) }
            }
        }

    @Test
    fun `시간 모름을 켜면 고른 시각이 지워지고 열린 시트가 닫힌다`() =
        runTest {
            val initial =
                OnboardingState(
                    step = OnboardingStep.BIRTH_INFO,
                    birthTime = BirthTime.JA,
                    sheet = OnboardingSheet.BIRTH_TIME,
                )
            viewModel().test(this, initialState = initial) {
                containerHost.changeBirthTimeUnknown(true)

                expectState {
                    copy(birthTime = BirthTime.UNKNOWN, sheet = null)
                }
            }
        }

    @Test
    fun `시간 모름을 켜면 시각을 고르지 않아도 다음으로 넘어갈 수 있다`() {
        val base =
            OnboardingState(
                step = OnboardingStep.BIRTH_INFO,
                gender = Gender.FEMALE,
                calendarType = DateType.SOLAR,
                birthDate = LocalDate.of(1999, 2, 13),
            )

        assertFalse(base.canProceed)
        assertTrue(base.copy(birthTime = BirthTime.UNKNOWN).canProceed)
        assertTrue(base.copy(birthTime = BirthTime.JA).canProceed)
    }

    @Test
    fun `첫 스텝에서 뒤로가면 약관 화면으로 이동한다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.NAME)
            viewModel().test(this, initialState = initial) {
                containerHost.clickBack()
                expectSideEffect(OnboardingSideEffect.NavigateToTerms)
            }
        }

    @Test
    fun `중간 스텝에서 뒤로가면 이전 스텝으로 돌아간다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.BIRTH_INFO)
            viewModel().test(this, initialState = initial) {
                containerHost.clickBack()

                expectState { copy(step = OnboardingStep.NAME) }
            }
        }

    @Test
    fun `완료 단계에서 다음을 누르면 권한 요청 사이드이펙트가 먼저 전달된다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.EXTRA_QUESTION)
            viewModel().test(this, initialState = initial) {
                containerHost.clickComplete()
                expectSideEffect(OnboardingSideEffect.PermissionRequest)
            }
        }

    @Test
    fun `가입 요청을 시작하면 응답을 기다리는 동안 완료 화면으로 전환된다`() =
        runTest {
            val initial =
                OnboardingState(
                    step = OnboardingStep.EXTRA_QUESTION,
                    username = UserName.Valid("토닥이"),
                    gender = Gender.FEMALE,
                    calendarType = DateType.SOLAR,
                    birthDate = LocalDate.of(1999, 2, 13),
                    birthTime = BirthTime.JA,
                    lifeStage = Job.STUDENT,
                    relationshipStatus = RelationshipStatus.SOLO,
                )
            viewModel().test(this, initialState = initial) {
                containerHost.confirmComplete(onboardingToken)
                expectState { copy(isSubmitting = true, step = OnboardingStep.COMPLETE) }
                expectState { copy(isSubmitting = false, step = OnboardingStep.COMPLETE) }
            }
        }

    @Test
    fun `제출 중에는 완료 요청이 다시 전송되지 않는다`() =
        runTest {
            val initial =
                OnboardingState(
                    step = OnboardingStep.EXTRA_QUESTION,
                    username = UserName.Valid("토닥이"),
                    gender = Gender.FEMALE,
                    calendarType = DateType.SOLAR,
                    birthDate = LocalDate.of(1999, 2, 13),
                    birthTime = BirthTime.JA,
                    lifeStage = Job.STUDENT,
                    relationshipStatus = RelationshipStatus.SOLO,
                    isSubmitting = true,
                )
            viewModel().test(this, initialState = initial) {
                containerHost.confirmComplete(onboardingToken)
                expectNoItems()
            }
            assertEquals(0, authRepository.signupCallCount)
        }

    @Test
    fun `회원가입이 실패하면 원인과 함께 실패 사이드이펙트가 전달된다`() =
        runTest {
            val error = IllegalStateException("네트워크 오류")
            authRepository.signupResult = Result.failure(error)
            val initial =
                OnboardingState(
                    step = OnboardingStep.EXTRA_QUESTION,
                    username = UserName.Valid("토닥이"),
                    gender = Gender.FEMALE,
                    calendarType = DateType.SOLAR,
                    birthDate = LocalDate.of(1999, 2, 13),
                    birthTime = BirthTime.JA,
                    lifeStage = Job.STUDENT,
                    relationshipStatus = RelationshipStatus.SOLO,
                )
            viewModel().test(this, initialState = initial) {
                containerHost.confirmComplete(onboardingToken)
                expectState { copy(isSubmitting = true, step = OnboardingStep.COMPLETE) }
                expectState { copy(isSubmitting = false, step = OnboardingStep.EXTRA_QUESTION) }
                expectSideEffect(OnboardingSideEffect.Failure(error))
            }
        }
}
