package com.kikidan.home

import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.model.fortune.LuckActionDetail
import com.kikidan.domain.model.fortune.TodayFortune
import com.kikidan.domain.usecase.GetDailyFortuneDetailUseCase
import com.kikidan.domain.usecase.GetHomeFortuneUseCase
import com.kikidan.domain.usecase.GetLuckActionDetailUseCase
import com.kikidan.home.model.CategoryScoreUiModel
import com.kikidan.home.model.DetailSheetUiState
import com.kikidan.home.model.FortuneReportState
import com.kikidan.home.model.HomeSideEffect
import com.kikidan.home.model.HomeState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.LocalDate

class HomeViewModelTest {
    @Test
    fun `load 성공 시 Success 상태로 전환되고 totalScore와 categories가 채워진다`() =
        runTest {
            val fakeFortuneRepository =
                FakeFortuneRepository().apply {
                    todayFortuneResult =
                        Result.success(
                            TodayFortune(
                                id = "f-today",
                                date = LocalDate.now(),
                                totalScore = 72,
                                scoreLabel = "흐름 좋은 날",
                                scores =
                                    listOf(
                                        FortuneScore(FortuneCategory.RELATIONSHIP, 45, "la-1"),
                                        FortuneScore(FortuneCategory.LOVE, 84, "la-2"),
                                        FortuneScore(FortuneCategory.ACHIEVEMENT, 38, "la-3"),
                                        FortuneScore(FortuneCategory.HEALTH, 21, "la-4"),
                                        FortuneScore(FortuneCategory.MONEY, 72, "la-5"),
                                    ),
                            ),
                        )
                }
            val vm = viewModel(fakeFortuneRepository, FakeLuckActionRepository())

            vm.test(this) {
                containerHost.load()
                val success = awaitState() as HomeState.Success
                assertEquals(72, success.totalScore)
                assertEquals("흐름 좋은 날", success.scoreLabel)
                assertEquals(5, success.categories.size)
                assertNull(success.detail)
            }
        }

    @Test
    fun `load 성공 시 categories는 FortuneCategory 선언 순서로 정렬된다`() =
        runTest {
            val fakeFortuneRepository =
                FakeFortuneRepository().apply {
                    todayFortuneResult =
                        Result.success(
                            TodayFortune(
                                id = "f-today",
                                date = LocalDate.now(),
                                totalScore = 60,
                                scoreLabel = "보통",
                                scores =
                                    listOf(
                                        FortuneScore(FortuneCategory.HEALTH, 21, "la-4"),
                                        FortuneScore(FortuneCategory.MONEY, 72, "la-5"),
                                        FortuneScore(FortuneCategory.RELATIONSHIP, 45, "la-1"),
                                    ),
                            ),
                        )
                }
            val vm = viewModel(fakeFortuneRepository, FakeLuckActionRepository())

            vm.test(this) {
                containerHost.load()
                val success = awaitState() as HomeState.Success
                assertEquals(
                    listOf(FortuneCategory.RELATIONSHIP, FortuneCategory.MONEY, FortuneCategory.HEALTH),
                    success.categories.map { it.category },
                )
            }
        }

    @Test
    fun `load 실패 시 Error 사이드이펙트와 Failure 상태가 발행된다`() =
        runTest {
            val fakeFortuneRepository =
                FakeFortuneRepository().apply {
                    todayFortuneResult = Result.failure(IllegalStateException("network error"))
                }
            val vm = viewModel(fakeFortuneRepository, FakeLuckActionRepository())

            vm.test(this) {
                containerHost.load()
                val se = awaitSideEffect()
                assertTrue(se is HomeSideEffect.Error)
                val failure = awaitState()
                assertTrue(failure is HomeState.Failure)
            }
        }

    @Test
    fun `openDetail 성공 시 detail이 Loading에서 Success로 전이된다`() =
        runTest {
            val detail =
                LuckActionDetail(
                    id = "la-1",
                    category = FortuneCategory.LOVE,
                    score = 84,
                    title = "사랑 액션",
                    content = "오늘 소중한 사람에게 연락해보세요",
                    achieved = false,
                )
            val fakeLuckActionRepository =
                FakeLuckActionRepository().apply { luckActionDetailResult = Result.success(detail) }
            val vm = viewModel(FakeFortuneRepository(), fakeLuckActionRepository)
            val initial =
                HomeState.Success(
                    totalScore = 72,
                    scoreLabel = "흐름 좋은 날",
                    categories = persistentListOf(CategoryScoreUiModel("la-1", FortuneCategory.LOVE, 84)),
                )

            vm.test(this, initialState = initial) {
                containerHost.openDetail("la-1")
                val loading = awaitState() as HomeState.Success
                assertTrue(loading.detail is DetailSheetUiState.Loading)
                assertEquals(FortuneCategory.LOVE, (loading.detail as DetailSheetUiState.Loading).category)
                val success = awaitState() as HomeState.Success
                val sheetSuccess = success.detail as DetailSheetUiState.Success
                assertEquals(84, sheetSuccess.score)
                assertEquals("사랑 액션", sheetSuccess.actionTitle)
                assertEquals("오늘 소중한 사람에게 연락해보세요", sheetSuccess.content)
            }
        }

    @Test
    fun `openDetail 실패 시 Error 사이드이펙트와 detail null로 복귀한다`() =
        runTest {
            val fakeLuckActionRepository =
                FakeLuckActionRepository().apply {
                    luckActionDetailResult = Result.failure(IllegalStateException())
                }
            val vm = viewModel(FakeFortuneRepository(), fakeLuckActionRepository)
            val initial =
                HomeState.Success(
                    totalScore = 72,
                    scoreLabel = "흐름 좋은 날",
                    categories = persistentListOf(CategoryScoreUiModel("la-1", FortuneCategory.LOVE, 84)),
                )

            vm.test(this, initialState = initial) {
                containerHost.openDetail("la-1")
                awaitState() // Loading detail
                val se = awaitSideEffect()
                assertTrue(se is HomeSideEffect.Error)
                val settled = awaitState() as HomeState.Success
                assertNull(settled.detail)
            }
        }

    @Test
    fun `closeDetail 호출 시 detail이 null이 되고 나머지 상태는 유지된다`() =
        runTest {
            val vm = viewModel(FakeFortuneRepository(), FakeLuckActionRepository())
            val initial =
                HomeState.Success(
                    totalScore = 72,
                    scoreLabel = "흐름 좋은 날",
                    categories = persistentListOf(CategoryScoreUiModel("la-1", FortuneCategory.LOVE, 84)),
                    detail = DetailSheetUiState.Loading(FortuneCategory.LOVE),
                )

            vm.test(this, initialState = initial) {
                containerHost.closeDetail()
                val settled = awaitState() as HomeState.Success
                assertNull(settled.detail)
                assertEquals(72, settled.totalScore)
                assertEquals(1, settled.categories.size)
            }
        }

    @Test
    fun `Success 상태에서 openDetail 재호출 시 기존 totalScore와 categories가 초기화되지 않는다`() =
        runTest {
            val detail1 =
                LuckActionDetail("la-2", FortuneCategory.MONEY, 72, "금전 액션", "절약해보세요", false)
            val fakeLuckActionRepository =
                FakeLuckActionRepository().apply { luckActionDetailResult = Result.success(detail1) }
            val vm = viewModel(FakeFortuneRepository(), fakeLuckActionRepository)
            val categories =
                persistentListOf(
                    CategoryScoreUiModel("la-1", FortuneCategory.LOVE, 84),
                    CategoryScoreUiModel("la-2", FortuneCategory.MONEY, 72),
                )
            val initial =
                HomeState.Success(
                    totalScore = 72,
                    scoreLabel = "흐름 좋은 날",
                    categories = categories,
                )

            vm.test(this, initialState = initial) {
                containerHost.openDetail("la-2")
                awaitState() // Loading
                val success = awaitState() as HomeState.Success
                assertEquals(72, success.totalScore)
                assertEquals(categories, success.categories)
                assertTrue(success.detail is DetailSheetUiState.Success)
            }
        }

    @Test
    fun `Success 상태가 아닐 때 closeDetail은 아무것도 하지 않는다`() =
        runTest {
            val vm = viewModel(FakeFortuneRepository(), FakeLuckActionRepository())

            vm.test(this, initialState = HomeState.Loading) {
                containerHost.closeDetail()
                expectNoItems()
            }
        }

    @Test
    fun `openDetail 진행 중 load가 state를 갱신해도 reduce는 최신 totalScore를 보존한다`() =
        runTest {
            val deferred = CompletableDeferred<Result<LuckActionDetail>>()
            val refreshedFortune =
                TodayFortune(
                    id = "f-refreshed",
                    date = LocalDate.now(),
                    totalScore = 99,
                    scoreLabel = "아주 좋은 날",
                    scores = listOf(FortuneScore(FortuneCategory.LOVE, 84, "la-1")),
                )
            val fakeFortuneRepository =
                FakeFortuneRepository().apply {
                    todayFortuneResult =
                        Result.success(refreshedFortune)
                }
            val fakeLuckActionRepository = FakeLuckActionRepository().apply { detailDeferred = deferred }
            val vm = viewModel(fakeFortuneRepository, fakeLuckActionRepository)
            val initialState =
                HomeState.Success(
                    totalScore = 72,
                    scoreLabel = "흐름 좋은 날",
                    categories = persistentListOf(CategoryScoreUiModel("la-1", FortuneCategory.LOVE, 84)),
                )
            val detail = LuckActionDetail("la-1", FortuneCategory.LOVE, 84, "사랑 액션", "내용", false)

            vm.test(this, initialState = initialState) {
                // openDetail 시작 — 네트워크 대기 중
                containerHost.openDetail("la-1")
                awaitState() // detail = Loading

                // 대기 중 load() 완료 → totalScore=99로 갱신
                containerHost.load()
                awaitState() // totalScore=99, detail=null

                // openDetail 응답 도착 — reduce는 최신 state(totalScore=99)를 기준으로 동작해야 함
                deferred.complete(Result.success(detail))
                val finalState = awaitState() as HomeState.Success
                assertEquals(99, finalState.totalScore)
                assertTrue(finalState.detail is DetailSheetUiState.Success)
            }
        }

    private fun viewModel(
        fakeFortuneRepository: FakeFortuneRepository,
        fakeLuckActionRepository: FakeLuckActionRepository,
    ): HomeViewModel =
        HomeViewModel(
            getHomeFortune = GetHomeFortuneUseCase(fakeFortuneRepository),
            getLuckActionDetail = GetLuckActionDetailUseCase(fakeLuckActionRepository),
        )
}
