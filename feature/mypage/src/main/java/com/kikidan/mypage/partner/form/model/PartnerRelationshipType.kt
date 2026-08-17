package com.kikidan.mypage.partner.form.model

import androidx.annotation.StringRes
import com.kikidan.designsystem.R

enum class PartnerRelationshipType(
    val code: String,
    @param:StringRes val labelRes: Int,
) {
    LOVER("LOVER", R.string.partner_relationship_lover),
    FRIEND("FRIEND", R.string.partner_relationship_friend),
    COLLEAGUE("COLLEAGUE", R.string.partner_relationship_colleague),
}
