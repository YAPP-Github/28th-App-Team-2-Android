package com.kikidan.sajucontents

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.usecase.GetDayFortuneUseCase
import com.kikidan.sajucontents.fake.FakeDayFortuneRepository
import com.kikidan.sajucontents.model.DateFortuneResultSideEffect
import kotlinx.collections.immutable.persistentListOf
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
class DateFortuneResultViewModelTest {
    private lateinit var fakeRepository: FakeDayFortuneRepository
    private lateinit var getDayFortune: GetDayFortuneUseCase

    @Before
    fun setUp() {
        // DateFortuneResultViewModel의 container(...)는 viewModelScope(Dispatchers.Main.immediate)를 쓴다.
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepository = FakeDayFortuneRepository()
        getDayFortune = GetDayFortuneUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = DateFortuneResultViewModel(getDayFortune)

    private fun fortune(id: String) =
        DayFortune(
            id = id,
            purpose = DayFortunePurpose.TRAVEL,
            targetDate = LocalDate.now(),
            score = 85,
            title = "title-$id",
            content = "content-$id",
            categoryStars = emptyList(),
        )

    @Test
    fun `여러_id를_요청한_순서대로_results에_매핑한다`() =
        runTest {
            val viewModel = viewModel()
            fakeRepository.getResults["id-1"] = Result.success(fortune("id-1"))
            fakeRepository.getResults["id-2"] = Result.success(fortune("id-2"))

            viewModel.test(this) {
                viewModel.loadResults(listOf("id-1", "id-2"))
                expectState { copy(isLoading = true) }
                expectState {
                    copy(
                        isLoading = false,
                        results = persistentListOf(fortune("id-1"), fortune("id-2")),
                        selectedResultIndex = 0,
                    )
                }
            }
        }

    @Test
    fun `하나라도_실패하면_ShowError가_발생하고_isLoading만_false로_돌아온다`() =
        runTest {
            val viewModel = viewModel()
            fakeRepository.getResults["id-1"] = Result.success(fortune("id-1"))
            fakeRepository.getResults["id-2"] = Result.failure(IllegalStateException("서버 오류"))

            viewModel.test(this) {
                viewModel.loadResults(listOf("id-1", "id-2"))
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false) }
                expectSideEffect(DateFortuneResultSideEffect.ShowError)
            }
        }

    @Test
    fun `탭_선택_시_selectedResultIndex가_변경된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.selectTabResult(1)
                expectState { copy(selectedResultIndex = 1) }
            }
        }
}
