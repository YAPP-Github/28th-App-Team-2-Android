package com.kikidan.sajucontents

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.usecase.GetYearFortuneUseCase
import com.kikidan.sajucontents.fake.FakeYearFortuneRepository
import com.kikidan.sajucontents.model.YearFortuneResultSideEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class YearFortuneResultViewModelTest {
    private lateinit var fakeRepository: FakeYearFortuneRepository
    private lateinit var getYearFortune: GetYearFortuneUseCase

    @Before
    fun setUp() {
        // YearFortuneResultViewModel의 container(...)는 viewModelScope(Dispatchers.Main.immediate)를 쓴다.
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepository = FakeYearFortuneRepository()
        getYearFortune = GetYearFortuneUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = YearFortuneResultViewModel(getYearFortune)

    @Test
    fun `load_성공_시_fortuneResult가_설정된다`() =
        runTest {
            val viewModel = viewModel()
            val fortune =
                YearFortune(
                    id = "id-1",
                    year = 2026,
                    score = 85,
                    title = "title",
                    content = "content",
                    categories = listOf(FortuneCategoryStar(FortuneCategory.MONEY, 3)),
                )
            fakeRepository.result = Result.success(fortune)

            viewModel.test(this) {
                viewModel.load("id-1")
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false, fortuneResult = fortune) }
            }

            assertEquals("id-1", fakeRepository.lastId)
        }

    @Test
    fun `load_실패_시_isLoading이_false로_돌아오고_ShowError가_발생한다`() =
        runTest {
            val viewModel = viewModel()
            fakeRepository.result = Result.failure(IllegalStateException("서버 오류"))

            viewModel.test(this) {
                viewModel.load("id-1")
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false) }
                expectSideEffect(YearFortuneResultSideEffect.ShowError)
            }
        }

    @Test
    fun `공유_아이콘_탭_시_isShareDialogVisible이_true가_된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.showShareDialog()
                expectState { copy(isShareDialogVisible = true) }
            }
        }

    @Test
    fun `취소_시_isShareDialogVisible이_false가_된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.showShareDialog()
                expectState { copy(isShareDialogVisible = true) }
                viewModel.hideShareDialog()
                expectState { copy(isShareDialogVisible = false) }
            }
        }

    @Test
    fun `카카오_공유_실패_시_팝업이_닫히고_ShowShareError가_발생한다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.showShareDialog()
                expectState { copy(isShareDialogVisible = true) }
                viewModel.notifyShareUnavailable()
                expectState { copy(isShareDialogVisible = false) }
                expectSideEffect(YearFortuneResultSideEffect.ShowShareError)
            }
        }

    @Test
    fun `URL_복사_시_팝업이_닫히고_ShowUrlCopied가_발생한다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.showShareDialog()
                expectState { copy(isShareDialogVisible = true) }
                viewModel.notifyUrlCopied()
                expectState { copy(isShareDialogVisible = false) }
                expectSideEffect(YearFortuneResultSideEffect.ShowUrlCopied)
            }
        }
}
