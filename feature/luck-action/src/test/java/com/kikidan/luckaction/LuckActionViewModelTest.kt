package com.kikidan.luckaction

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.usecase.GetEarliestFortuneDateUseCase
import com.kikidan.domain.usecase.GetLuckActionPageUseCase
import com.kikidan.domain.usecase.ToggleLuckActionUseCase
import com.kikidan.luckaction.model.LuckActionItemUiModel
import com.kikidan.luckaction.model.LuckActionSideEffect
import com.kikidan.luckaction.model.LuckActionUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.LocalDate

class LuckActionViewModelTest {
    @Test
    fun `load 성공 시 Success 상태로 전환되고 scores actions가 반영된다`() =
        runTest {
            val fakeFortuneRepository =
                FakeFortuneRepository().apply {
                    scoresResult = Result.success(listOf(FortuneScore(FortuneCategory.LOVE, 21)))
                    earliestDateResult = Result.success(LocalDate.now().minusDays(3))
                }
            val fakeLuckActionRepository =
                FakeLuckActionRepository().apply {
                    actionsResult = Result.success(listOf(LuckAction("1", FortuneCategory.LOVE, "제목", false)))
                }
            val vm = viewModel(fakeFortuneRepository, fakeLuckActionRepository)

            vm.test(this) {
                containerHost.load()
                val loaded = awaitState()
                assertTrue(loaded is LuckActionUiState.Success)
                loaded as LuckActionUiState.Success
                assertFalse(loaded.isRefreshing)
                assertEquals(1, loaded.scores.size)
                assertEquals(1, loaded.actions.size)
                assertFalse(loaded.actions[0].achieved)
                assertTrue(loaded.canGoToPrevDate)
            }
        }

    @Test
    fun `load 성공 시 오늘이 조회 가능한 가장 오래된 날짜면 canGoToPrevDate가 false다`() =
        runTest {
            val today = LocalDate.now()
            val fakeFortuneRepository =
                FakeFortuneRepository().apply {
                    scoresResult = Result.success(emptyList())
                    earliestDateResult = Result.success(today)
                }
            val fakeLuckActionRepository =
                FakeLuckActionRepository().apply { actionsResult = Result.success(emptyList()) }
            val vm = viewModel(fakeFortuneRepository, fakeLuckActionRepository)

            vm.test(this) {
                containerHost.load()
                val loaded = awaitState() as LuckActionUiState.Success
                assertFalse(loaded.canGoToPrevDate)
            }
        }

    @Test
    fun `load 실패 시 Failure 상태가 되고 Error 사이드이펙트가 발생한다`() =
        runTest {
            val fakeFortuneRepository =
                FakeFortuneRepository().apply { scoresResult = Result.failure(IllegalStateException()) }
            val vm = viewModel(fakeFortuneRepository, FakeLuckActionRepository())

            vm.test(this) {
                containerHost.load()
                val se = awaitSideEffect()
                assertTrue(se is LuckActionSideEffect.Error)
                val s = awaitState()
                assertTrue(s is LuckActionUiState.Failure)
            }
        }

    @Test
    fun `onPrevDateClick은 하루 전 날짜로 다시 조회한다`() =
        runTest {
            val today = LocalDate.now()
            val fakeFortuneRepository =
                FakeFortuneRepository().apply {
                    recordResult = Result.success(FortuneRecord(scores = emptyList(), actions = emptyList()))
                    earliestDateResult = Result.success(today.minusDays(5))
                }
            val vm = viewModel(fakeFortuneRepository, FakeLuckActionRepository())
            val initial = LuckActionUiState.Success(date = today, canGoToPrevDate = true)

            vm.test(this, initialState = initial) {
                containerHost.onPrevDateClick()
                val refreshing = awaitState() as LuckActionUiState.Success
                assertTrue(refreshing.isRefreshing)
                assertEquals(today, refreshing.date)
                val loaded = awaitState() as LuckActionUiState.Success
                assertFalse(loaded.isRefreshing)
                assertEquals(today.minusDays(1), loaded.date)
                assertTrue(loaded.canGoToPrevDate)
            }
        }

    @Test
    fun `onPrevDateClick 조회가 실패하면 Success 상태를 유지하고 date와 목록은 그대로다`() =
        runTest {
            val today = LocalDate.now()
            val fakeFortuneRepository =
                FakeFortuneRepository().apply { recordResult = Result.failure(IllegalStateException()) }
            val staleActions = persistentListOf(LuckActionItemUiModel("1", FortuneCategory.LOVE, "오늘 액션", false))
            val vm = viewModel(fakeFortuneRepository, FakeLuckActionRepository())
            val initial = LuckActionUiState.Success(date = today, canGoToPrevDate = true, actions = staleActions)

            vm.test(this, initialState = initial) {
                containerHost.onPrevDateClick()
                awaitState() // isRefreshing = true, date/actions는 아직 그대로
                val se = awaitSideEffect()
                assertTrue(se is LuckActionSideEffect.Error)
                val failed = awaitState() as LuckActionUiState.Success
                assertFalse(failed.isRefreshing)
                assertEquals(today, failed.date)
                assertEquals(staleActions, failed.actions)
            }
        }

    @Test
    fun `onPrevDateClick이 방어적으로 no-record를 만나면 canGoToPrevDate만 false로 바뀐다`() =
        runTest {
            val today = LocalDate.now()
            val fakeFortuneRepository = FakeFortuneRepository().apply { recordResult = Result.success(null) }
            val staleActions = persistentListOf(LuckActionItemUiModel("1", FortuneCategory.LOVE, "오늘 액션", false))
            val vm = viewModel(fakeFortuneRepository, FakeLuckActionRepository())
            val initial = LuckActionUiState.Success(date = today, canGoToPrevDate = true, actions = staleActions)

            vm.test(this, initialState = initial) {
                containerHost.onPrevDateClick()
                awaitState() // isRefreshing = true
                val settled = awaitState() as LuckActionUiState.Success
                assertFalse(settled.isRefreshing)
                assertFalse(settled.canGoToPrevDate)
                assertEquals(today, settled.date)
                assertEquals(staleActions, settled.actions)
            }
        }

    @Test
    fun `canGoToPrevDate가 false면 onPrevDateClick은 아무 것도 하지 않는다`() =
        runTest {
            val vm = viewModel(FakeFortuneRepository(), FakeLuckActionRepository())
            val initial = LuckActionUiState.Success(date = LocalDate.now(), canGoToPrevDate = false)

            vm.test(this, initialState = initial) {
                containerHost.onPrevDateClick()
                expectNoItems()
            }
        }

    @Test
    fun `onNextDateClick은 오늘 날짜에서는 무시된다`() =
        runTest {
            val vm = viewModel(FakeFortuneRepository(), FakeLuckActionRepository())
            val initial = LuckActionUiState.Success(date = LocalDate.now(), canGoToPrevDate = true)

            vm.test(this, initialState = initial) {
                containerHost.onNextDateClick()
                expectNoItems()
            }
        }

    @Test
    fun `액션을 토글해서 achieved가 true가 되면 완료 오버레이 카테고리가 채워진다`() =
        runTest {
            val fakeLuckActionRepository =
                FakeLuckActionRepository().apply {
                    toggleResult = Result.success(LuckAction("1", FortuneCategory.LOVE, "제목", achieved = true))
                }
            val vm = viewModel(FakeFortuneRepository(), fakeLuckActionRepository)
            val initial =
                LuckActionUiState.Success(
                    date = LocalDate.now(),
                    canGoToPrevDate = true,
                    actions = persistentListOf(LuckActionItemUiModel("1", FortuneCategory.LOVE, "제목", false)),
                )

            vm.test(this, initialState = initial) {
                containerHost.onToggleAction("1")
                val s = awaitState() as LuckActionUiState.Success
                assertTrue(s.actions[0].achieved)
                assertEquals(FortuneCategory.LOVE, s.completionOverlayCategory)
            }
        }

    @Test
    fun `완료 오버레이를 닫으면 completionOverlayCategory가 null이 된다`() =
        runTest {
            val vm = viewModel(FakeFortuneRepository(), FakeLuckActionRepository())
            val initial =
                LuckActionUiState.Success(
                    date = LocalDate.now(),
                    canGoToPrevDate = true,
                    completionOverlayCategory = FortuneCategory.LOVE,
                )

            vm.test(this, initialState = initial) {
                containerHost.onCompleteOverlayDismiss()
                val s = awaitState() as LuckActionUiState.Success
                assertNull(s.completionOverlayCategory)
            }
        }

    private fun viewModel(
        fakeFortuneRepository: FakeFortuneRepository,
        fakeLuckActionRepository: FakeLuckActionRepository,
    ): LuckActionViewModel =
        LuckActionViewModel(
            getLuckActionPage = GetLuckActionPageUseCase(fakeFortuneRepository, fakeLuckActionRepository),
            getEarliestFortuneDate = GetEarliestFortuneDateUseCase(fakeFortuneRepository),
            toggleLuckAction = ToggleLuckActionUseCase(fakeLuckActionRepository),
        )
}
