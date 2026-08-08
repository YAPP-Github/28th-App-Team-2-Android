package com.kikidan.onboarding

import com.kikidan.domain.model.onboarding.OnboardingTerm
import com.kikidan.onboarding.model.TermsAgreementUiModel
import com.kikidan.onboarding.model.TermsDialog
import com.kikidan.onboarding.model.TermsSideEffect
import com.kikidan.onboarding.model.TermsState
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class TermsViewModelTest {
    private fun viewModel() = TermsViewModel()

    @Before
    fun setUp() {
        // viewModelScope가 Dispatchers.Main을 요구하므로 테스트 디스패처로 바꿔둔다.
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `필수 약관을 모두 동의해야 진행할 수 있다`() =
        runTest {
            viewModel().test(this) {
                containerHost.changeTerm(OnboardingTerm.SERVICE)
                assertFalse(awaitState().canProceed)

                containerHost.changeTerm(OnboardingTerm.PRIVACY)
                awaitState()
                containerHost.changeTerm(OnboardingTerm.AI_DATA_TRANSFER)
                assertTrue(awaitState().canProceed)
            }
        }

    @Test
    fun `선택 약관은 진행 여부에 영향을 주지 않는다`() {
        val terms =
            TermsAgreementUiModel(
                persistentSetOf(
                    OnboardingTerm.SERVICE,
                    OnboardingTerm.PRIVACY,
                    OnboardingTerm.AI_DATA_TRANSFER,
                ),
            )

        assertTrue(TermsState(termsAgreement = terms).canProceed)
        assertFalse(TermsState(termsAgreement = terms.toggle(OnboardingTerm.PRIVACY)).canProceed)
    }

    @Test
    fun `전체 동의를 켜면 모든 항목이 켜지고 끄면 모두 꺼진다`() =
        runTest {
            viewModel().test(this) {
                containerHost.changeAllTerms(true)
                expectState {
                    copy(termsAgreement = TermsAgreementUiModel(OnboardingTerm.entries.toPersistentSet()))
                }

                containerHost.changeAllTerms(false)
                expectState { copy(termsAgreement = TermsAgreementUiModel()) }
            }
        }

    @Test
    fun `항목 하나를 해제하면 전체 동의 상태가 풀린다`() =
        runTest {
            viewModel().test(this) {
                containerHost.changeAllTerms(true)
                skipItems(1)

                containerHost.changeTerm(OnboardingTerm.MARKETING)
                val state = awaitState()

                assertFalse(state.termsAgreement.allSelected)
                assertTrue(state.termsAgreement.allRequiredSelected)
            }
        }

    @Test
    fun `뒤로가면 이탈 확인 다이얼로그가 뜬다`() =
        runTest {
            viewModel().test(this) {
                containerHost.clickBack()
                expectState { copy(dialog = TermsDialog.EXIT_CONFIRM) }

                containerHost.confirmExit()
                expectState { TermsState() }
                expectSideEffect(TermsSideEffect.Exit)
            }
        }

    @Test
    fun `다이얼로그를 닫으면 상태가 사라진다`() =
        runTest {
            viewModel().test(this) {
                containerHost.clickBack()
                expectState { copy(dialog = TermsDialog.EXIT_CONFIRM) }

                containerHost.dismissDialog()
                expectState { copy(dialog = null) }
            }
        }
}
