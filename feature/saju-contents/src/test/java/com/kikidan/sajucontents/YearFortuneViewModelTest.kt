package com.kikidan.sajucontents

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.usecase.GetYearFortuneUseCase
import com.kikidan.sajucontents.fake.FakeYearFortuneRepository
import com.kikidan.sajucontents.model.YearFortuneSideEffect
import com.kikidan.sajucontents.model.YearFortuneState
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
class YearFortuneViewModelTest {
    private lateinit var fakeRepository: FakeYearFortuneRepository
    private lateinit var getYearFortune: GetYearFortuneUseCase

    @Before
    fun setUp() {
        // YearFortuneViewModel의 container(...)는 viewModelScope(Dispatchers.Main.immediate)를 쓴다.
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepository = FakeYearFortuneRepository()
        getYearFortune = GetYearFortuneUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = YearFortuneViewModel(getYearFortune)

    @Test
    fun `연도_선택_시_selectedYear가_변경된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.onYearSelect(2030)
                expectState { copy(selectedYear = 2030) }
            }
        }

    @Test
    fun `제출_성공_시_fortuneResult가_반영되고_NavigateToResult가_발생한다`() =
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
            val initial = YearFortuneState(selectedYear = 2026)

            viewModel.test(this, initialState = initial) {
                viewModel.onSubmit()
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false, fortuneResult = fortune) }
                expectSideEffect(YearFortuneSideEffect.NavigateToResult(2026))
            }

            assertEquals(2026, fakeRepository.lastYear)
        }

    @Test
    fun `UseCase_실패_시_isLoading이_false로_돌아오고_error가_설정된다`() =
        runTest {
            val viewModel = viewModel()
            fakeRepository.result = Result.failure(IllegalStateException("서버 오류"))
            val initial = YearFortuneState(selectedYear = 2026)

            viewModel.test(this, initialState = initial) {
                viewModel.onSubmit()
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false, error = LOAD_ERROR_MESSAGE) }
            }
        }

    @Test
    fun `공유_아이콘_클릭과_닫기로_isShareSheetVisible이_토글된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.onShareIconClick()
                expectState { copy(isShareSheetVisible = true) }

                viewModel.onShareSheetDismiss()
                expectState { copy(isShareSheetVisible = false) }
            }
        }

    @Test
    fun `load_성공_시_selectedYear와_fortuneResult가_설정되고_SideEffect는_발생하지_않는다`() =
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
                viewModel.load(2026)
                expectState { copy(selectedYear = 2026, isLoading = true) }
                expectState { copy(isLoading = false, fortuneResult = fortune) }
            }

            assertEquals(2026, fakeRepository.lastYear)
        }

    @Test
    fun `load_실패_시_error가_설정되고_fortuneResult는_null로_유지된다`() =
        runTest {
            val viewModel = viewModel()
            fakeRepository.result = Result.failure(IllegalStateException("서버 오류"))

            viewModel.test(this) {
                viewModel.load(2026)
                expectState { copy(selectedYear = 2026, isLoading = true) }
                expectState { copy(isLoading = false, error = LOAD_ERROR_MESSAGE) }
            }
        }
}
