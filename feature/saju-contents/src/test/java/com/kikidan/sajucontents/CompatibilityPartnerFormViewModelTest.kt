package com.kikidan.sajucontents

import com.kikidan.domain.usecase.saju.RegisterPartnerSajuUseCase
import com.kikidan.sajucontents.fake.FakePartnerSajuRepository
import com.kikidan.sajucontents.model.CompatibilityPartnerFormSideEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CompatibilityPartnerFormViewModelTest {
    private lateinit var fakeRepository: FakePartnerSajuRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepository = FakePartnerSajuRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = CompatibilityPartnerFormViewModel(RegisterPartnerSajuUseCase(fakeRepository))

    @Test
    fun `이름이_10자를_초과하면_에러가_설정된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.updateName("가나다라마바사아자차카")
                expectState {
                    copy(name = "가나다라마바사아자차카", nameErrorMessageRes = R.string.compatibility_partner_form_name_error)
                }
            }
        }

    @Test
    fun `필수값이_모두_채워지면_저장이_가능하다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.updateName("토실이")
                expectState { copy(name = "토실이") }
                viewModel.updateBirthDate(LocalDate.of(2001, 5, 30))
                expectState { copy(name = "토실이", birthDate = LocalDate.of(2001, 5, 30)) }
            }
        }

    @Test
    fun `저장_성공_시_뒤로가기_사이드이펙트가_발생한다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.updateName("토실이")
                expectState { copy(name = "토실이") }
                viewModel.updateBirthDate(LocalDate.of(2001, 5, 30))
                expectState { copy(name = "토실이", birthDate = LocalDate.of(2001, 5, 30)) }

                viewModel.save()
                expectState { copy(name = "토실이", birthDate = LocalDate.of(2001, 5, 30), isSaving = true) }
                expectState { copy(name = "토실이", birthDate = LocalDate.of(2001, 5, 30), isSaving = false) }
                expectSideEffect(CompatibilityPartnerFormSideEffect.NavigateBack)
            }
            org.junit.Assert.assertEquals("토실이", fakeRepository.lastRegisterInput?.name)
        }

    @Test
    fun `저장_실패_시_에러_사이드이펙트가_발생한다`() =
        runTest {
            fakeRepository.registerResult = Result.failure(IllegalStateException("서버 오류"))
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.updateName("토실이")
                expectState { copy(name = "토실이") }
                viewModel.updateBirthDate(LocalDate.of(2001, 5, 30))
                expectState { copy(name = "토실이", birthDate = LocalDate.of(2001, 5, 30)) }

                viewModel.save()
                expectState { copy(name = "토실이", birthDate = LocalDate.of(2001, 5, 30), isSaving = true) }
                expectState { copy(name = "토실이", birthDate = LocalDate.of(2001, 5, 30), isSaving = false) }
                expectSideEffect(CompatibilityPartnerFormSideEffect.ShowSaveError)
            }
        }
}
