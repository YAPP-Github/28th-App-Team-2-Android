package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.saju.SajuPillarType
import com.kikidan.domain.usecase.CreateCompatibilityUseCase
import com.kikidan.domain.usecase.mypage.GetMansaeryeokDetailUseCase
import com.kikidan.domain.usecase.saju.GetPartnerMansaeryeokDetailUseCase
import com.kikidan.domain.usecase.saju.GetPartnerSajuListUseCase
import com.kikidan.sajucontents.model.CompatibilityEntrySideEffect
import com.kikidan.sajucontents.model.CompatibilityInputState
import com.kikidan.sajucontents.model.CreateCompatibilityState
import com.kikidan.sajucontents.model.MyInfoLoadState
import com.kikidan.sajucontents.model.PartnerListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import kotlin.fold

@HiltViewModel
class CompatibilityInputViewModel
    @Inject
    constructor(
        private val getMansaeryeokDetail: GetMansaeryeokDetailUseCase,
        private val getPartnerSajuList: GetPartnerSajuListUseCase,
        private val getPartnerMansaeryeokDetail: GetPartnerMansaeryeokDetailUseCase,
        private val createCompatibility: CreateCompatibilityUseCase,
    ) : ViewModel(),
        ContainerHost<CompatibilityInputState, CompatibilityEntrySideEffect> {
        override val container: Container<CompatibilityInputState, CompatibilityEntrySideEffect> =
            container(CompatibilityInputState())

        fun loadMyInfo() =
            intent {
                reduce { state.copy(myInfoState = MyInfoLoadState.Loading) }
                coroutineScope {
                    val mansaeryeokDetailDeferred = async { getMansaeryeokDetail() }
                    val partnerListDeferred = async { getPartnerSajuList() }
                    val mansaeryeokDetail = mansaeryeokDetailDeferred.await()
                    val partnerList = partnerListDeferred.await()

                    runCatching {
                        val detail = mansaeryeokDetail.getOrThrow()
                        val list = partnerList.getOrThrow()
                        detail to list
                    }.fold(
                        onSuccess = { (detail, list) ->
                            reduce {
                                state.copy(
                                    myInfoState = MyInfoLoadState.Success,
                                    myUser = detail.user,
                                    myPillars =
                                        PillarDisplayOrder
                                            .mapNotNull { type ->
                                                detail.chart.pillars.firstOrNull {
                                                    it.pillarType ==
                                                        type
                                                }
                                            }.toPersistentList(),
                                    partnerPicker =
                                        state.partnerPicker.copy(
                                            partnersState = PartnerListState.Success(list.toPersistentList()),
                                        ),
                                )
                            }

                            // 상대방 추가 후 되돌아왔을 때 선택이 되어있게끔
                            if (list.size == 1) {
                                selectPartner(list.first().linkId)
                            }
                        },
                        onFailure = {
                            reduce { state.copy(myInfoState = MyInfoLoadState.Failure) }
                            postSideEffect(CompatibilityEntrySideEffect.ShowLoadError)
                        },
                    )
                }
            }

        fun openPartnerPicker() =
            intent {
                val partnersLoading = state.partnerPicker.partnersState is PartnerListState.Loading
                val myInfoLoading = state.myInfoState is MyInfoLoadState.Loading
                if (partnersLoading || myInfoLoading) return@intent
                if (state.partnerPicker.partners.isEmpty()) {
                    postSideEffect(CompatibilityEntrySideEffect.NavigateToPartnerForm)
                    return@intent
                }
                reduce { state.copy(partnerPicker = state.partnerPicker.copy(isVisible = true)) }
            }

        fun dismissPartnerPicker() =
            intent {
                reduce { state.copy(partnerPicker = state.partnerPicker.copy(isVisible = false)) }
            }

        fun addNewPartner() =
            intent {
                reduce { state.copy(partnerPicker = state.partnerPicker.copy(isVisible = false)) }
                postSideEffect(CompatibilityEntrySideEffect.NavigateToPartnerForm)
            }

        fun selectPartner(linkId: String) =
            intent {
                getPartnerMansaeryeokDetail(linkId)
                    .onSuccess { detail ->
                        reduce {
                            state.copy(
                                selectedPartner = detail.partner,
                                selectedPartnerPillars =
                                    PillarDisplayOrder
                                        .mapNotNull { type ->
                                            detail.chart.pillars.firstOrNull {
                                                it.pillarType ==
                                                    type
                                            }
                                        }.toPersistentList(),
                                partnerPicker = state.partnerPicker.copy(isVisible = false),
                            )
                        }
                    }.onFailure {
                        reduce { state.copy(partnerPicker = state.partnerPicker.copy(isVisible = false)) }
                        postSideEffect(CompatibilityEntrySideEffect.ShowLoadError)
                    }
            }

        fun checkCompatibility() =
            intent {
                val partnerLinkId = state.selectedPartner?.linkId ?: return@intent
                if (state.createState is CreateCompatibilityState.Loading) return@intent
                reduce { state.copy(createState = CreateCompatibilityState.Loading) }
                createCompatibility(partnerLinkId)
                    .onSuccess { compatibility ->
                        reduce { state.copy(createState = CreateCompatibilityState.Success) }
                        postSideEffect(
                            CompatibilityEntrySideEffect.NavigateToResult(compatibility.id, partnerLinkId),
                        )
                    }.onFailure {
                        reduce { state.copy(createState = CreateCompatibilityState.Failure) }
                        postSideEffect(CompatibilityEntrySideEffect.ShowCreateError)
                    }
            }

        companion object {
            private val PillarDisplayOrder =
                listOf(SajuPillarType.HOUR, SajuPillarType.DAY, SajuPillarType.MONTH, SajuPillarType.YEAR)
        }
    }
