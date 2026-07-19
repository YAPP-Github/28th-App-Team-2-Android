package com.kikidan.designsystem.component.bottomnavigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kikidan.designsystem.R

enum class TodakunNavItem(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val selectedIconRes: Int,
    @param:DrawableRes val unselectedIconRes: Int,
    //TODO Navigation Route 클래스를 프로퍼티에 추가
) {
    LUCKY(
        labelRes = R.string.bottom_nav_fortune,
        selectedIconRes = R.drawable.ic_navi_lucky_on,
        unselectedIconRes = R.drawable.ic_navi_lucky_off,
    ),
    AI(
        labelRes = R.string.bottom_nav_todak,
        selectedIconRes = R.drawable.ic_navi_ai_on,
        unselectedIconRes = R.drawable.ic_navi_ai_off,
    ),
    ACTION(
        labelRes = R.string.bottom_nav_lucky_action,
        selectedIconRes = R.drawable.ic_navi_action_on,
        unselectedIconRes = R.drawable.ic_navi_action_off,
    ),
    MY(
        labelRes = R.string.bottom_nav_my,
        selectedIconRes = R.drawable.ic_navi_my_on,
        unselectedIconRes = R.drawable.ic_navi_my_off,
    ),
}
