package com.kikidan.home.model

import androidx.compose.ui.graphics.Color
import com.kikidan.designsystem.theme.TodakunColor

internal enum class FortuneScoreBand(
    val start: Color,
    val end: Color,
) {
    LOW(TodakunColor.red200, TodakunColor.red300),
    MID_LOW(TodakunColor.teal200, TodakunColor.teal400),
    MID_HIGH(TodakunColor.sky300, TodakunColor.sky500),
    HIGH(TodakunColor.sky500, TodakunColor.primary600),
    ;

    companion object {
        fun of(score: Int) =
            when {
                score <= 35 -> LOW
                score <= 65 -> MID_LOW
                score <= 80 -> MID_HIGH
                else -> HIGH
            }
    }
}
