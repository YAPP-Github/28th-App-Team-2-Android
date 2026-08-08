package com.kikidan.onboarding

import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.domain.model.onboarding.OnboardingTerm
import com.kikidan.domain.model.onboarding.UserName
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.SignUpUseCase
import com.kikidan.onboarding.fake.FakeAuthRepository
import com.kikidan.onboarding.fake.FakeTokenRepository
import com.kikidan.onboarding.model.OnboardingDialog
import com.kikidan.onboarding.model.OnboardingSheet
import com.kikidan.onboarding.model.OnboardingSideEffect
import com.kikidan.onboarding.model.OnboardingState
import com.kikidan.onboarding.model.OnboardingStep
import com.kikidan.onboarding.model.TermsAgreementUiModel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import kotlinx.collections.immutable.toPersistentSet
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
    fun `필수 약관을 모두 동의해야 다음으로 넘어갈 수 있다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.TERMS)
            viewModel().test(this, initialState = initial) {
                containerHost.onTermChange(OnboardingTerm.SERVICE)
                expectState { copy(termsAgreement = termsAgreement.toggle(OnboardingTerm.SERVICE)) }

                containerHost.onNextClick()
                expectNoItems()

                containerHost.onTermChange(OnboardingTerm.PRIVACY)
                expectState { copy(termsAgreement = termsAgreement.toggle(OnboardingTerm.PRIVACY)) }
                containerHost.onTermChange(OnboardingTerm.AI_DATA_TRANSFER)
                expectState { copy(termsAgreement = termsAgreement.toggle(OnboardingTerm.AI_DATA_TRANSFER)) }

                containerHost.onNextClick()
                expectState { copy(step = OnboardingStep.NAME) }
            }
        }

    @Test
    fun `선택 약관은 다음 단계 진행에 영향을 주지 않는다`() =
        runTest {
            val terms =
                TermsAgreementUiModel(
                    persistentSetOf(
                        OnboardingTerm.SERVICE,
                        OnboardingTerm.PRIVACY,
                        OnboardingTerm.AI_DATA_TRANSFER,
                    ),
                )

            assertTrue(OnboardingState(step = OnboardingStep.TERMS, termsAgreement = terms).canProceed)
            assertFalse(
                OnboardingState(
                    step = OnboardingStep.TERMS,
                    termsAgreement = terms.toggle(OnboardingTerm.PRIVACY),
                ).canProceed,
            )
        }

    @Test
    fun `전체 동의를 켜면 모든 항목이 켜지고 끄면 모두 꺼진다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.TERMS)
            viewModel().test(this, initialState = initial) {
                containerHost.onAllTermsChange(true)
                expectState {
                    copy(termsAgreement = TermsAgreementUiModel(OnboardingTerm.entries.toPersistentSet()))
                }

                containerHost.onAllTermsChange(false)
                expectState { copy(termsAgreement = TermsAgreementUiModel()) }
            }
        }

    @Test
    fun `항목 하나를 해제하면 전체 동의 상태가 풀린다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.TERMS)
            viewModel().test(this, initialState = initial) {
                containerHost.onAllTermsChange(true)
                skipItems(1)

                containerHost.onTermChange(OnboardingTerm.MARKETING)
                val state = awaitState()

                assertFalse(state.termsAgreement.allSelected)
                assertTrue(state.termsAgreement.allRequiredSelected)
            }
        }

    @Test
    fun `특수문자가 든 이름은 에러로 표시되고 다음으로 넘어갈 수 없다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.NAME)
            viewModel().test(this, initialState = initial) {
                containerHost.onNameChange("토닥이##")
                expectState {
                    copy(username = UserName.Invalid.ContainsSpecialCharacter("토닥이##"))
                }

                containerHost.onNextClick()
                expectNoItems()
            }
        }

    @Test
    fun `유효한 이름을 넣으면 다음 스텝으로 넘어간다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.NAME)
            viewModel().test(this, initialState = initial) {
                containerHost.onNameChange("토닥이")
                expectState { copy(username = UserName.Valid("토닥이")) }

                containerHost.onNextClick()
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
                containerHost.onBirthTimeUnknownChange(true)

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
    fun `약관 동의 스텝에서 뒤로가면 이탈 확인 다이얼로그가 뜬다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.TERMS)
            viewModel().test(this, initialState = initial) {
                containerHost.onBackClick()
                expectState { copy(dialog = OnboardingDialog.EXIT_CONFIRM) }

                containerHost.onExitConfirmed()
                expectState { OnboardingState() }
                expectSideEffect(OnboardingSideEffect.Exit)
            }
        }

    @Test
    fun `중간 스텝에서 뒤로가면 이전 스텝으로 돌아간다`() =
        runTest {
            val initial = OnboardingState(step = OnboardingStep.BIRTH_INFO)
            viewModel().test(this, initialState = initial) {
                containerHost.onBackClick()

                expectState { copy(step = OnboardingStep.NAME) }
            }
        }

    @Test
    fun `마지막 스텝에서 다음을 누르면 완료 화면으로 전환된다`() =
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
                containerHost.onCompleteConfirmed(onboardingToken)
                expectState { copy(isSubmitting = true) }
                expectState { copy(isSubmitting = false, step = OnboardingStep.COMPLETE) }
                expectSideEffect(OnboardingSideEffect.PermissionRequest)
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
                containerHost.onCompleteConfirmed(onboardingToken)
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
                containerHost.onCompleteConfirmed(onboardingToken)
                expectState { copy(isSubmitting = true) }
                expectState { copy(isSubmitting = false) }
                expectSideEffect(OnboardingSideEffect.Failure(error))
            }
        }
}
