package com.kikidan.mypage.home.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor

enum class MyPageMenuType(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val showChevron: Boolean = true,
    val iconTint: Color = TodakunColor.gray975,
    val textColor: Color = TodakunColor.black,
) {
    SAJU_INFO(iconRes = R.drawable.ic_manage_saju_info, labelRes = R.string.mypage_menu_saju_info),
    NOTIFICATION_SETTING(iconRes = R.drawable.ic_bell, labelRes = R.string.mypage_menu_notification_setting),
    APP_SETTING(iconRes = R.drawable.ic_setting, labelRes = R.string.mypage_menu_app_setting),
    INQUIRY(iconRes = R.drawable.ic_mail, labelRes = R.string.mypage_menu_inquiry),
    LOGOUT(
        iconRes = R.drawable.ic_logout,
        labelRes = R.string.mypage_menu_logout,
        showChevron = false,
        iconTint = TodakunColor.gray500,
        textColor = TodakunColor.gray600,
    ),
}
