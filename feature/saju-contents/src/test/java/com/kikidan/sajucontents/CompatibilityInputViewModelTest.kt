package com.kikidan.sajucontents

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.RelationshipType
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.CreateCompatibilityUseCase
import com.kikidan.domain.usecase.mypage.GetMansaeryeokDetailUseCase
import com.kikidan.domain.usecase.saju.GetPartnerMansaeryeokDetailUseCase
import com.kikidan.domain.usecase.saju.GetPartnerSajuChartDetailUseCase
import com.kikidan.domain.usecase.saju.GetPartnerSajuListUseCase
import com.kikidan.domain.usecase.saju.GetPartnerSajuUseCase
import com.kikidan.domain.usecase.saju.GetSajuChartDetailUseCase
import com.kikidan.domain.usecase.user.GetUserUseCase
import com.kikidan.sajucontents.fake.FakeCompatibilityRepository
import com.kikidan.sajucontents.fake.FakePartnerSajuRepository
import com.kikidan.sajucontents.fake.FakeSajuRepository
import com.kikidan.sajucontents.fake.FakeUserRepository
import com.kikidan.sajucontents.model.CompatibilityEntrySideEffect
import com.kikidan.sajucontents.model.CreateCompatibilityState
import com.kikidan.sajucontents.model.MyInfoLoadState
import com.kikidan.sajucontents.model.PartnerListState
import kotlinx.collections.immutable.persistentListOf
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
class CompatibilityInputViewModelTest {
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeSajuRepository: FakeSajuRepository
    private lateinit var fakePartnerSajuRepository: FakePartnerSajuRepository
    private lateinit var fakeCompatibilityRepository: FakeCompatibilityRepository

    @Before
    fun setUp() {
        // CompatibilityInputViewModel의 container(...)는 viewModelScope(Dispatchers.Main.immediate)를 쓴다.
        Dispatchers.setMain(StandardTestDispatcher())
        fakeUserRepository = FakeUserRepository()
        fakeSajuRepository = FakeSajuRepository()
        fakePartnerSajuRepository = FakePartnerSajuRepository()
        fakeCompatibilityRepository = FakeCompatibilityRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        CompatibilityInputViewModel(
            getMansaeryeokDetail =
                GetMansaeryeokDetailUseCase(
                    GetUserUseCase(fakeUserRepository),
                    GetSajuChartDetailUseCase(fakeSajuRepository),
                ),
            getPartnerSajuList = GetPartnerSajuListUseCase(fakePartnerSajuRepository),
            getPartnerMansaeryeokDetail =
                GetPartnerMansaeryeokDetailUseCase(
                    GetPartnerSajuUseCase(fakePartnerSajuRepository),
                    GetPartnerSajuChartDetailUseCase(fakePartnerSajuRepository),
                ),
            createCompatibility = CreateCompatibilityUseCase(fakeCompatibilityRepository),
        )

    @Test
    fun `초기화_시_내_정보와_파트너_목록이_로드된다`() =
        runTest {
            fakePartnerSajuRepository.listResult = Result.success(emptyList())
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.loadMyInfo()
                expectState { copy(myInfoState = MyInfoLoadState.Loading) }
                expectState {
                    copy(
                        myInfoState = MyInfoLoadState.Success,
                        myUser = fakeUserRepository.result.getOrThrow(),
                        myPillars = persistentListOf(),
                        partnerPicker =
                            partnerPicker.copy(partnersState = PartnerListState.Success(persistentListOf())),
                    )
                }
            }
        }

    @Test
    fun `파트너가_한_명뿐이면_자동으로_선택된다`() =
        runTest {
            val viewModel = viewModel()
            val partners = fakePartnerSajuRepository.listResult.getOrThrow()

            viewModel.test(this) {
                viewModel.loadMyInfo()
                expectState { copy(myInfoState = MyInfoLoadState.Loading) }
                expectState {
                    copy(
                        myInfoState = MyInfoLoadState.Success,
                        myUser = fakeUserRepository.result.getOrThrow(),
                        myPillars = persistentListOf(),
                        partnerPicker =
                            partnerPicker.copy(
                                partnersState = PartnerListState.Success(persistentListOf(*partners.toTypedArray())),
                            ),
                    )
                }
                expectState {
                    copy(
                        selectedPartner = fakePartnerSajuRepository.getResult.getOrThrow(),
                        selectedPartnerPillars = persistentListOf(),
                        partnerPicker = partnerPicker.copy(isVisible = false),
                    )
                }
            }
        }

    @Test
    fun `파트너가_있으면_선택_시트가_열린다`() =
        runTest {
            val viewModel = viewModel()
            val partners = fakePartnerSajuRepository.listResult.getOrThrow()

            viewModel.test(this) {
                viewModel.loadMyInfo()
                expectState { copy(myInfoState = MyInfoLoadState.Loading) }
                expectState {
                    copy(
                        myInfoState = MyInfoLoadState.Success,
                        myUser = fakeUserRepository.result.getOrThrow(),
                        myPillars = persistentListOf(),
                        partnerPicker =
                            partnerPicker.copy(
                                partnersState = PartnerListState.Success(persistentListOf(*partners.toTypedArray())),
                            ),
                    )
                }
                // 파트너가 한 명이라 자동 선택된다.
                expectState {
                    copy(
                        selectedPartner = fakePartnerSajuRepository.getResult.getOrThrow(),
                        selectedPartnerPillars = persistentListOf(),
                        partnerPicker = partnerPicker.copy(isVisible = false),
                    )
                }

                viewModel.openPartnerPicker()
                expectState { copy(partnerPicker = partnerPicker.copy(isVisible = true)) }
            }
        }

    @Test
    fun `파트너가_없으면_바로_등록_화면으로_이동한다`() =
        runTest {
            fakePartnerSajuRepository.listResult = Result.success(emptyList())
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.loadMyInfo()
                expectState { copy(myInfoState = MyInfoLoadState.Loading) }
                expectState {
                    copy(
                        myInfoState = MyInfoLoadState.Success,
                        myUser = fakeUserRepository.result.getOrThrow(),
                        myPillars = persistentListOf(),
                        partnerPicker =
                            partnerPicker.copy(partnersState = PartnerListState.Success(persistentListOf())),
                    )
                }

                viewModel.openPartnerPicker()
                expectSideEffect(CompatibilityEntrySideEffect.NavigateToPartnerForm)
            }
        }

    @Test
    fun `궁합_확인_성공_시_결과_화면으로_이동한다`() =
        runTest {
            fakePartnerSajuRepository.listResult = Result.success(emptyList())
            fakePartnerSajuRepository.getResult =
                Result.success(
                    PartnerSaju(
                        linkId = "partner-1",
                        name = "토실이",
                        gender = Gender.MALE,
                        relationshipType = RelationshipType("LOVER", "연인"),
                        birth = Birth(DateType.SOLAR, LocalDate.of(2001, 5, 30), BirthTime.O),
                    ),
                )
            fakePartnerSajuRepository.chartDetailResult =
                Result.success(SajuChartDetail(CheonGan.GI, emptyList(), emptyList()))
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.loadMyInfo()
                expectState { copy(myInfoState = MyInfoLoadState.Loading) }
                expectState {
                    copy(
                        myInfoState = MyInfoLoadState.Success,
                        myUser = fakeUserRepository.result.getOrThrow(),
                        myPillars = persistentListOf(),
                        partnerPicker =
                            partnerPicker.copy(partnersState = PartnerListState.Success(persistentListOf())),
                    )
                }

                viewModel.selectPartner("partner-1")
                expectState {
                    copy(
                        selectedPartner = fakePartnerSajuRepository.getResult.getOrThrow(),
                        selectedPartnerPillars = persistentListOf(),
                        partnerPicker = partnerPicker.copy(isVisible = false),
                    )
                }

                viewModel.checkCompatibility()
                expectState { copy(createState = CreateCompatibilityState.Loading) }
                expectState { copy(createState = CreateCompatibilityState.Success) }
                expectSideEffect(CompatibilityEntrySideEffect.NavigateToResult("compat-1", "partner-1"))
            }
            assertEquals("partner-1", fakeCompatibilityRepository.lastPartnerLinkId)
        }

    @Test
    fun `궁합_확인_실패_시_에러가_발생한다`() =
        runTest {
            fakePartnerSajuRepository.listResult = Result.success(emptyList())
            fakePartnerSajuRepository.getResult =
                Result.success(
                    PartnerSaju(
                        linkId = "partner-1",
                        name = "토실이",
                        gender = Gender.MALE,
                        relationshipType = RelationshipType("LOVER", "연인"),
                        birth = Birth(DateType.SOLAR, LocalDate.of(2001, 5, 30), BirthTime.O),
                    ),
                )
            fakePartnerSajuRepository.chartDetailResult =
                Result.success(SajuChartDetail(CheonGan.GI, emptyList(), emptyList()))
            fakeCompatibilityRepository.result = Result.failure(IllegalStateException("서버 오류"))
            val viewModel = viewModel()

            viewModel.test(this) {
                viewModel.loadMyInfo()
                expectState { copy(myInfoState = MyInfoLoadState.Loading) }
                expectState {
                    copy(
                        myInfoState = MyInfoLoadState.Success,
                        myUser = fakeUserRepository.result.getOrThrow(),
                        myPillars = persistentListOf(),
                        partnerPicker =
                            partnerPicker.copy(partnersState = PartnerListState.Success(persistentListOf())),
                    )
                }

                viewModel.selectPartner("partner-1")
                expectState {
                    copy(
                        selectedPartner = fakePartnerSajuRepository.getResult.getOrThrow(),
                        selectedPartnerPillars = persistentListOf(),
                        partnerPicker = partnerPicker.copy(isVisible = false),
                    )
                }

                viewModel.checkCompatibility()
                expectState { copy(createState = CreateCompatibilityState.Loading) }
                expectState { copy(createState = CreateCompatibilityState.Failure) }
                expectSideEffect(CompatibilityEntrySideEffect.ShowCreateError)
            }
        }
}
