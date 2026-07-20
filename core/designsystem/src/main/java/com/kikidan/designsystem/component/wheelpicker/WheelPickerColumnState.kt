package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.runtime.Immutable

@Immutable
data class WheelPickerColumnState(
    val items: List<String>,
    val selectedIndex: Int,
    val maxInputDigits: Int = 2,
)
