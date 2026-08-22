package com.kikidan.sajucontents

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.usecase.GetDayFortuneUseCase
import com.kikidan.sajucontents.fake.FakeDayFortuneRepository
import com.kikidan.sajucontents.model.DateFortuneResultLoadState
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

    private fun fortune(
        id: String,
        score: Int = 85,
    ) = DayFortune(
        id = id,
        purpose = DayFortunePurpose.TRAVEL,
        targetDate = LocalDate.now(),
        score = score,
        title = "title-$id",
        content = "content-$id",
        categoryStars = emptyList(),
    )

    @Test
    fun `결과가_3개_이하면_전부_점수_내림차순으로_남는다`() =
        runTest {
            val viewModel = viewModel()
            fakeRepository.getResults["id-1"] = Result.success(fortune("id-1", score = 70))
            fakeRepository.getResults["id-2"] = Result.success(fortune("id-2", score = 95))

            viewModel.test(this) {
                viewModel.loadResults(listOf("id-1", "id-2"))
                expectState { copy(resultState = DateFortuneResultLoadState.Loading) }
                expectState {
                    copy(
                        resultState =
                            DateFortuneResultLoadState.Success(
                                persistentListOf(fortune("id-2", score = 95), fortune("id-1", score = 70)),
                            ),
                        selectedResultIndex = 0,
                    )
                }
            }
        }

    @Test
    fun `결과가_3개를_초과하면_점수_내림차순_상위_3개만_남는다`() =
        runTest {
            val viewModel = viewModel()
            val ids = listOf("id-1", "id-2", "id-3", "id-4")
            fakeRepository.getResults["id-1"] = Result.success(fortune("id-1", score = 70))
            fakeRepository.getResults["id-2"] = Result.success(fortune("id-2", score = 95))
            fakeRepository.getResults["id-3"] = Result.success(fortune("id-3", score = 60))
            fakeRepository.getResults["id-4"] = Result.success(fortune("id-4", score = 80))

            viewModel.test(this) {
                viewModel.loadResults(ids)
                expectState { copy(resultState = DateFortuneResultLoadState.Loading) }
                expectState {
                    copy(
                        resultState =
                            DateFortuneResultLoadState.Success(
                                persistentListOf(
                                    fortune("id-2", score = 95),
                                    fortune("id-4", score = 80),
                                    fortune("id-1", score = 70),
                                ),
                            ),
                        selectedResultIndex = 0,
                    )
                }
            }
        }

    @Test
    fun `점수가_같으면_먼저_요청한_id_순서가_유지된다`() =
        runTest {
            val viewModel = viewModel()
            val ids = listOf("id-1", "id-2", "id-3")
            fakeRepository.getResults["id-1"] = Result.success(fortune("id-1", score = 80))
            fakeRepository.getResults["id-2"] = Result.success(fortune("id-2", score = 80))
            fakeRepository.getResults["id-3"] = Result.success(fortune("id-3", score = 80))

            viewModel.test(this) {
                viewModel.loadResults(ids)
                expectState { copy(resultState = DateFortuneResultLoadState.Loading) }
                expectState {
                    copy(
                        resultState =
                            DateFortuneResultLoadState.Success(
                                persistentListOf(
                                    fortune("id-1", score = 80),
                                    fortune("id-2", score = 80),
                                    fortune("id-3", score = 80),
                                ),
                            ),
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
                expectState { copy(resultState = DateFortuneResultLoadState.Loading) }
                expectState { copy(resultState = DateFortuneResultLoadState.Failure) }
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
                expectSideEffect(DateFortuneResultSideEffect.ShowShareError)
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
                expectSideEffect(DateFortuneResultSideEffect.ShowUrlCopied)
            }
        }
}
