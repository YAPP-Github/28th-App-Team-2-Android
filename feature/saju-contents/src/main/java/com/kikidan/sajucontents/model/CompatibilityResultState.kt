package com.kikidan.sajucontents.model

import com.kikidan.domain.model.compatibility.Compatibility
import com.kikidan.domain.model.saju.SajuPillarDetail
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

sealed interface CompatibilityResultState {
    data object Loading : CompatibilityResultState

    data class Success(
        val compatibilityResult: Compatibility,
        val myPillars: PersistentList<SajuPillarDetail> = persistentListOf(),
        val partnerPillars: PersistentList<SajuPillarDetail> = persistentListOf(),
        val isShareDialogVisible: Boolean = false,
    ) : CompatibilityResultState

    data object Failure : CompatibilityResultState
}

sealed interface CompatibilityResultSideEffect {
    data object ShowError : CompatibilityResultSideEffect

    data object ShowShareError : CompatibilityResultSideEffect

    data object ShowUrlCopied : CompatibilityResultSideEffect
}
