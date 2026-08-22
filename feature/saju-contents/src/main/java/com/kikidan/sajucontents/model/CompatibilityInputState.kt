package com.kikidan.sajucontents.model

import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.domain.model.user.User
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class CompatibilityInputState(
    val myInfoState: MyInfoLoadState? = null,
    val myUser: User? = null,
    val myPillars: PersistentList<SajuPillarDetail> = persistentListOf(),
    val selectedPartner: PartnerSaju? = null,
    val selectedPartnerPillars: PersistentList<SajuPillarDetail> = persistentListOf(),
    val createState: CreateCompatibilityState? = null,
    val partnerPicker: PartnerPickerState = PartnerPickerState(),
)

sealed interface MyInfoLoadState {
    data object Loading : MyInfoLoadState

    data object Success : MyInfoLoadState

    data object Failure : MyInfoLoadState
}

sealed interface CreateCompatibilityState {
    data object Loading : CreateCompatibilityState

    data object Success : CreateCompatibilityState

    data object Failure : CreateCompatibilityState
}

data class PartnerPickerState(
    val isVisible: Boolean = false,
    val partnersState: PartnerListState = PartnerListState.Loading,
) {
    val partners: PersistentList<PartnerSaju>
        get() = (partnersState as? PartnerListState.Success)?.partners ?: persistentListOf()
}

sealed interface PartnerListState {
    data object Loading : PartnerListState

    data class Success(
        val partners: PersistentList<PartnerSaju>,
    ) : PartnerListState

    data object Failure : PartnerListState
}
