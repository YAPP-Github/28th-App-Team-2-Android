package com.kikidan.sajucontents

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.usecase.GetCompatibilityUseCase
import com.kikidan.domain.usecase.saju.GetPartnerSajuChartDetailUseCase
import com.kikidan.domain.usecase.saju.GetSajuChartDetailUseCase
import com.kikidan.sajucontents.fake.FakeCompatibilityRepository
import com.kikidan.sajucontents.fake.FakePartnerSajuRepository
import com.kikidan.sajucontents.fake.FakeSajuRepository
import com.kikidan.sajucontents.model.CompatibilityResultSideEffect
import com.kikidan.sajucontents.model.CompatibilityResultState
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

@OptIn(ExperimentalCoroutinesApi::class)
class CompatibilityResultViewModelTest {
    private lateinit var fakeCompatibilityRepository: FakeCompatibilityRepository
    private lateinit var fakeSajuRepository: FakeSajuRepository
    private lateinit var fakePartnerSajuRepository: FakePartnerSajuRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeCompatibilityRepository = FakeCompatibilityRepository()
        fakeSajuRepository = FakeSajuRepository()
        fakePartnerSajuRepository = FakePartnerSajuRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        CompatibilityResultViewModel(
            getCompatibility = GetCompatibilityUseCase(fakeCompatibilityRepository),
            getSajuChartDetail = GetSajuChartDetailUseCase(fakeSajuRepository),
            getPartnerSajuChartDetail = GetPartnerSajuChartDetailUseCase(fakePartnerSajuRepository),
        )

    @Test
    fun `partnerLinkId가_있으면_사주_구조까지_로드된다`() =
        runTest {
            fakeSajuRepository.chartDetailResult =
                Result.success(SajuChartDetail(CheonGan.GI, emptyList(), emptyList()))
            fakePartnerSajuRepository.chartDetailResult =
                Result.success(SajuChartDetail(CheonGan.SIN, emptyList(), emptyList()))
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.load("compat-1", "partner-1")
                expectState {
                    CompatibilityResultState.Success(
                        compatibilityResult = fakeCompatibilityRepository.result.getOrThrow(),
                    )
                }
            }
        }

    @Test
    fun `partnerLinkId가_없으면_사주_구조_없이_로드된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.load("compat-1", null)
                expectState {
                    CompatibilityResultState.Success(
                        compatibilityResult = fakeCompatibilityRepository.result.getOrThrow(),
                    )
                }
            }
        }

    @Test
    fun `조회_실패_시_ShowError가_발생한다`() =
        runTest {
            fakeCompatibilityRepository.result = Result.failure(IllegalStateException("서버 오류"))
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.load("compat-1", null)
                expectState { CompatibilityResultState.Failure }
                expectSideEffect(CompatibilityResultSideEffect.ShowError)
            }
        }

    @Test
    fun `공유_다이얼로그_상태가_토글된다`() =
        runTest {
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.load("compat-1", null)
                expectState {
                    CompatibilityResultState.Success(
                        compatibilityResult = fakeCompatibilityRepository.result.getOrThrow(),
                    )
                }

                viewModel.showShareDialog()
                expectState {
                    CompatibilityResultState.Success(
                        compatibilityResult = fakeCompatibilityRepository.result.getOrThrow(),
                        isShareDialogVisible = true,
                    )
                }
                viewModel.hideShareDialog()
                expectState {
                    CompatibilityResultState.Success(
                        compatibilityResult = fakeCompatibilityRepository.result.getOrThrow(),
                        isShareDialogVisible = false,
                    )
                }
            }
        }
}
