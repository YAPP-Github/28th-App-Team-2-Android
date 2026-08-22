package com.kikidan.sajucontents.model

import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.domain.model.user.User
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class CompatibilityInputState(
    val isLoading: Boolean = false,
    val myUser: User? = null,
    val myPillars: PersistentList<SajuPillarDetail> = persistentListOf(),
    val selectedPartner: PartnerSaju? = null,
    val selectedPartnerPillars: PersistentList<SajuPillarDetail> = persistentListOf(),
    val isCreating: Boolean = false,
    val partnerPicker: PartnerPickerState = PartnerPickerState(),
)

data class PartnerPickerState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val partners: PersistentList<PartnerSaju> = persistentListOf(),
)
