package com.kikidan.sajucontents.model

import androidx.annotation.StringRes
import com.kikidan.designsystem.R

enum class CompatibilityRelationshipType(
    val code: String,
    @param:StringRes val labelRes: Int,
) {
    LOVER("LOVER", R.string.partner_relationship_lover),
    FRIEND("FRIEND", R.string.partner_relationship_friend),
    COLLEAGUE("COLLEAGUE", R.string.partner_relationship_colleague),
}
