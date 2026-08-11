package com.kikidan.sajucontents

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.CreateDayFortunesUseCase
import com.kikidan.domain.usecase.DateFortuneDefaults
import com.kikidan.sajucontents.fake.FakeDayFortuneRepository
import com.kikidan.sajucontents.model.DateFortuneSideEffect
import com.kikidan.sajucontents.model.DateFortuneState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DateFortuneViewModelTest {
    private lateinit var fakeRepository: FakeDayFortuneRepository
    private lateinit var createDayFortunes: CreateDayFortunesUseCase

    @Before
    fun setUp() {
        // DateFortuneViewModel의 container(...)는 viewModelScope(Dispatchers.Main.immediate)를 쓴다.
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepository = FakeDayFortuneRepository()
        createDayFortunes = CreateDayFortunesUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = DateFortuneViewModel(createDayFortunes)

    @Test
    fun `목적_선택_시_selectedPurpose가_변경된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.selectPurpose(DayFortunePurpose.TRAVEL)
                expectState { copy(selectedPurpose = DayFortunePurpose.TRAVEL) }
            }
        }

    @Test
    fun `날짜_토글로_추가와_제거가_selectedDates에_반영된다`() =
        runTest {
            val viewModel = viewModel()
            val date = LocalDate.now()

            viewModel.test(this) {
                viewModel.toggleDate(date)
                expectState { copy(selectedDates = persistentListOf(date)) }

                viewModel.toggleDate(date)
                expectState { copy(selectedDates = persistentListOf()) }
            }
        }

    @Test
    fun `5개_선택된_상태에서_추가_시도하면_상태는_불변이고_ShowToast가_발생한다`() =
        runTest {
            val viewModel = viewModel()
            val today = LocalDate.now()
            val fiveDates = (0 until DateFortuneDefaults.MAX_TARGET_DATES).map { today.plusDays(it.toLong()) }
            val initial = DateFortuneState(selectedDates = fiveDates.toPersistentList())

            viewModel.test(this, initialState = initial) {
                viewModel.toggleDate(today.plusDays(10))
                expectSideEffect(DateFortuneSideEffect.ShowToast(R.string.date_fortune_max_dates_toast))
            }
        }

    @Test
    fun `초기화하면_selectedDates가_비워진다`() =
        runTest {
            val viewModel = viewModel()
            val initial =
                DateFortuneState(
                    selectedPurpose = DayFortunePurpose.TRAVEL,
                    selectedDates = persistentListOf(LocalDate.now()),
                )

            viewModel.test(this, initialState = initial) {
                viewModel.reset()
                expectState { copy(selectedDates = persistentListOf()) }
            }
        }

    @Test
    fun `성별_선택_시_selectedGender가_변경된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.selectGender(Gender.FEMALE)
                expectState { copy(selectedGender = Gender.FEMALE) }
            }
        }

    @Test
    fun `제출하면_UseCase에는_purpose와_targetDates_2개_인자만_전달되고_gender는_새지_않는다`() =
        runTest {
            val viewModel = viewModel()
            val date = LocalDate.now()
            val initial =
                DateFortuneState(
                    selectedPurpose = DayFortunePurpose.TRAVEL,
                    selectedGender = Gender.MALE,
                    selectedDates = persistentListOf(date),
                )
            fakeRepository.result = Result.success(emptyList())

            viewModel.test(this, initialState = initial) {
                viewModel.submit()
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false, results = persistentListOf(), selectedResultIndex = 0) }
                expectSideEffect(DateFortuneSideEffect.NavigateToResult)
            }

            // CreateDayFortunesUseCase.invoke(purpose, targetDates) 시그니처 자체가 2-arity라 컴파일 타임에도
            // gender가 섞일 수 없다. Fake Repository로 실제 전달값까지 한 번 더 고정한다 (B-2 회귀 방지).
            assertEquals(DayFortunePurpose.TRAVEL, fakeRepository.lastPurpose)
            assertEquals(listOf(date), fakeRepository.lastTargetDates)
        }

    @Test
    fun `제출_성공_시_results와_selectedResultIndex가_반영된다`() =
        runTest {
            val viewModel = viewModel()
            val date = LocalDate.now()
            val fortune =
                DayFortune(
                    id = "id-1",
                    purpose = DayFortunePurpose.TRAVEL,
                    targetDate = date,
                    score = 85,
                    title = "title",
                    content = "content",
                    categoryStars = emptyList(),
                )
            fakeRepository.result = Result.success(listOf(fortune))
            val initial =
                DateFortuneState(
                    selectedPurpose = DayFortunePurpose.TRAVEL,
                    selectedDates = persistentListOf(date),
                )

            viewModel.test(this, initialState = initial) {
                viewModel.submit()
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false, results = persistentListOf(fortune), selectedResultIndex = 0) }
                expectSideEffect(DateFortuneSideEffect.NavigateToResult)
            }
        }

    @Test
    fun `UseCase_실패_시_isLoading이_false로_돌아오고_ShowError가_발생한다`() =
        runTest {
            val viewModel = viewModel()
            val date = LocalDate.now()
            fakeRepository.result = Result.failure(IllegalStateException("서버 오류"))
            val initial =
                DateFortuneState(
                    selectedPurpose = DayFortunePurpose.TRAVEL,
                    selectedDates = persistentListOf(date),
                )

            viewModel.test(this, initialState = initial) {
                viewModel.submit()
                expectState { copy(isLoading = true) }
                expectState { copy(isLoading = false) }
                expectSideEffect(DateFortuneSideEffect.ShowError(R.string.date_fortune_submit_error))
            }
        }

    @Test
    fun `결과_탭_전환_시_selectedResultIndex가_변경된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.selectTabResult(2)
                expectState { copy(selectedResultIndex = 2) }
            }
        }
}
