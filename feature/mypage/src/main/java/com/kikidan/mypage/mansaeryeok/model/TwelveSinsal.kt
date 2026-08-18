package com.kikidan.mypage.mansaeryeok.model

import androidx.annotation.StringRes
import com.kikidan.designsystem.R

enum class TwelveSinsal(
    @param:StringRes val labelRes: Int,
) {
    GEOPSAL(R.string.mansaeryeok_twelve_sinsal_geopsal),
    JAESAL(R.string.mansaeryeok_twelve_sinsal_jaesal),
    CHEONSAL(R.string.mansaeryeok_twelve_sinsal_cheonsal),
    JISAL(R.string.mansaeryeok_twelve_sinsal_jisal),
    NYEONSAL(R.string.mansaeryeok_twelve_sinsal_nyeonsal),
    WOLSAL(R.string.mansaeryeok_twelve_sinsal_wolsal),
    MANGSINSAL(R.string.mansaeryeok_twelve_sinsal_mangsinsal),
    JANGSEONGSAL(R.string.mansaeryeok_twelve_sinsal_jangseongsal),
    BANANSAL(R.string.mansaeryeok_twelve_sinsal_banansal),
    YEOKMASAL(R.string.mansaeryeok_twelve_sinsal_yeokmasal),
    YUKHAESAL(R.string.mansaeryeok_twelve_sinsal_yukhaesal),
    HWAGAESAL(R.string.mansaeryeok_twelve_sinsal_hwagaesal),
}
