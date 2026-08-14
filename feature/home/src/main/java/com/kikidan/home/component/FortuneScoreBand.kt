package com.kikidan.home.component

import androidx.compose.ui.graphics.Color
import com.kikidan.designsystem.theme.TodakunColor

internal enum class FortuneScoreBand(
    val start: Color,
    val end: Color,
) {
    LOW(TodakunColor.red300, TodakunColor.red500),
    MID(TodakunColor.teal500, TodakunColor.teal700),
    HIGH(TodakunColor.primary500, TodakunColor.primary700),
    ;

    companion object {
        fun of(score: Int) =
            when {
                score < 40 -> LOW
                score < 60 -> MID
                else -> HIGH
            }
    }
}
