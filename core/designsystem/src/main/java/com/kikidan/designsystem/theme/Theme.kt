package com.kikidan.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun TodakunTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalTodakunColor provides defaultTodakunColor,
    ) {
        content()
    }
}

object TodakunTheme {
    val colors: TodakunColor
        @Composable
        @ReadOnlyComposable
        get() = LocalTodakunColor.current

    val typography: TodakunTypography
        get() = TodakunTypography
}
