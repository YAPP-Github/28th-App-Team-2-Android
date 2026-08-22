package com.kikidan.mypage.mansaeryeok.model

import androidx.annotation.StringRes
import com.kikidan.designsystem.R

enum class TenGod(
    @param:StringRes val labelRes: Int,
) {
    ILWON(R.string.mansaeryeok_ten_god_ilwon),
    BIGYEON(R.string.mansaeryeok_ten_god_bigyeon),
    GEOPJAE(R.string.mansaeryeok_ten_god_gyeopjae),
    SIKSIN(R.string.mansaeryeok_ten_god_siksin),
    SANGGWAN(R.string.mansaeryeok_ten_god_sanggwan),
    PYEONJAE(R.string.mansaeryeok_ten_god_pyeonjae),
    JEONGJAE(R.string.mansaeryeok_ten_god_jeongjae),
    PYEONGWAN(R.string.mansaeryeok_ten_god_pyeongwan),
    JEONGGWAN(R.string.mansaeryeok_ten_god_jeonggwan),
    PYEONIN(R.string.mansaeryeok_ten_god_pyeonin),
    JEONGIN(R.string.mansaeryeok_ten_god_jeongin),
}
