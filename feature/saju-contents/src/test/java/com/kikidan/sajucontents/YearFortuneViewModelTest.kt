package com.kikidan.sajucontents

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.usecase.CreateYearFortuneUseCase
import com.kikidan.sajucontents.fake.FakeYearFortuneRepository
import com.kikidan.sajucontents.model.YearFortuneSideEffect
import com.kikidan.sajucontents.model.YearFortuneState
import com.kikidan.sajucontents.model.YearFortuneSubmitState
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
    private lateinit var createYearFortune: CreateYearFortuneUseCase

    @Before
    fun setUp() {
        // YearFortuneViewModel의 container(...)는 viewModelScope(Dispatchers.Main.immediate)를 쓴다.
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepository = FakeYearFortuneRepository()
        createYearFortune = CreateYearFortuneUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = YearFortuneViewModel(createYearFortune)

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
    fun `제출_성공_시_생성된_결과의_id로_NavigateToResult가_발생한다`() =
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
                expectState { copy(submitState = YearFortuneSubmitState.Loading) }
                expectState { copy(submitState = YearFortuneSubmitState.Success) }
                expectSideEffect(YearFortuneSideEffect.NavigateToResult("id-1"))
            }

            assertEquals(2026, fakeRepository.lastYear)
        }

    @Test
    fun `UseCase_실패_시_isLoading이_false로_돌아오고_ShowError가_발생한다`() =
        runTest {
            val viewModel = viewModel()
            fakeRepository.result = Result.failure(IllegalStateException("서버 오류"))
            val initial = YearFortuneState(selectedYear = 2026)

            viewModel.test(this, initialState = initial) {
                viewModel.onSubmit()
                expectState { copy(submitState = YearFortuneSubmitState.Loading) }
                expectState { copy(submitState = YearFortuneSubmitState.Failure) }
                expectSideEffect(YearFortuneSideEffect.ShowError)
            }
        }
}
