package com.kikidan.mypage.mansaeryeok.model

import androidx.annotation.StringRes
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunBadgeType

enum class FiveElementStatus(
    @param:StringRes val labelRes: Int,
    val badgeType: TodakunBadgeType,
) {
    LACKING(R.string.mansaeryeok_five_element_status_lacking, TodakunBadgeType.Gray),
    MODERATE(R.string.mansaeryeok_five_element_status_moderate, TodakunBadgeType.Green),
    ABUNDANT(R.string.mansaeryeok_five_element_status_abundant, TodakunBadgeType.Purple),
}
