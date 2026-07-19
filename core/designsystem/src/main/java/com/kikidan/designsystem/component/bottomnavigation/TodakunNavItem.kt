package com.kikidan.designsystem.component.bottomnavigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kikidan.designsystem.R

/**
 * [TodakunBottomNavigation]에서 사용하는 탭 식별자.
 *
 * enum 이름은 Figma 아이콘 컴포넌트명(`ic_navi_*`)에 맞춘다(예: [LUCKY] → "운세" 탭).
 * 실제 화면 라우팅과의 매핑은 이 모듈 밖(`app`/`core:navigation`)에서 수행한다.
 */
enum class TodakunNavItem(
    @StringRes val labelRes: Int,
    @DrawableRes val selectedIconRes: Int,
    @DrawableRes val unselectedIconRes: Int,
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
