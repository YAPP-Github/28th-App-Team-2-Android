package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetCompatibilityUseCase
import com.kikidan.domain.usecase.saju.GetPartnerSajuChartDetailUseCase
import com.kikidan.domain.usecase.saju.GetSajuChartDetailUseCase
import com.kikidan.sajucontents.model.CompatibilityResultSideEffect
import com.kikidan.sajucontents.model.CompatibilityResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

// id를 직접 전달받아 스스로 데이터를 불러온다. 공유 딥링크로 결과 화면에 바로 진입할 수 있도록
// 궁합 입력 화면(CompatibilityEntryViewModel)과 완전히 분리된 인스턴스로 존재한다.
@HiltViewModel
class CompatibilityResultViewModel
    @Inject
    constructor(
        private val getCompatibility: GetCompatibilityUseCase,
        private val getSajuChartDetail: GetSajuChartDetailUseCase,
        private val getPartnerSajuChartDetail: GetPartnerSajuChartDetailUseCase,
    ) : ViewModel(),
        ContainerHost<CompatibilityResultState, CompatibilityResultSideEffect> {
        override val container: Container<CompatibilityResultState, CompatibilityResultSideEffect> =
            container(CompatibilityResultState.Loading)

        fun load(
            compatibilityId: String,
            partnerLinkId: String?,
        ) = intent {
            reduce { CompatibilityResultState.Loading }

            getCompatibility(compatibilityId).fold(
                onSuccess = { result ->
                    val pillars = loadSajuStructure(partnerLinkId)
                    reduce {
                        CompatibilityResultState.Success(
                            compatibilityResult = result,
                            myPillars = pillars?.first?.toPersistentList() ?: persistentListOf(),
                            partnerPillars = pillars?.second?.toPersistentList() ?: persistentListOf(),
                        )
                    }
                },
                onFailure = {
                    reduce { CompatibilityResultState.Failure }
                    postSideEffect(CompatibilityResultSideEffect.ShowError)
                },
            )
        }

        private suspend fun loadSajuStructure(partnerLinkId: String?) =
            coroutineScope {
                if (partnerLinkId == null) return@coroutineScope null
                val myDeferred = async { getSajuChartDetail() }
                val partnerDeferred = async { getPartnerSajuChartDetail(partnerLinkId) }
                val my = myDeferred.await().getOrNull() ?: return@coroutineScope null
                val partner = partnerDeferred.await().getOrNull() ?: return@coroutineScope null
                my.pillars to partner.pillars
            }

        fun showShareDialog() = intent { reduceShareDialogVisible(true) }

        fun hideShareDialog() = intent { reduceShareDialogVisible(false) }

        fun notifyShareUnavailable() =
            intent {
                reduceShareDialogVisible(false)
                postSideEffect(CompatibilityResultSideEffect.ShowShareError)
            }

        fun notifyUrlCopied() =
            intent {
                reduceShareDialogVisible(false)
                postSideEffect(CompatibilityResultSideEffect.ShowUrlCopied)
            }

        private suspend fun Syntax<CompatibilityResultState, CompatibilityResultSideEffect>.reduceShareDialogVisible(
            isVisible: Boolean,
        ) = reduce {
            val current = state
            if (current is CompatibilityResultState.Success) {
                current.copy(isShareDialogVisible = isVisible)
            } else {
                current
            }
        }
    }
