package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.LocalTodakunColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WheelPickerBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    bottomSpacing: Dp = WheelPickerBottomSheetDefaults.BottomSpacing,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalTodakunColor.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = WheelPickerBottomSheetDefaults.Shape,
        containerColor = colors.white,
        dragHandle = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("wheel_picker_drag_handle"),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 4.dp)
                        .clip(WheelPickerBottomSheetDefaults.HandleShape)
                        .background(colors.gray200),
                )
            }
        },
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = WheelPickerBottomSheetDefaults.HorizontalMargin)
            .padding(bottom = bottomSpacing),
        content = content
    )
}

private object WheelPickerBottomSheetDefaults {
    val HorizontalMargin: Dp = 20.dp

    val BottomSpacing: Dp = 16.dp
    val Shape = RoundedCornerShape(12.dp)
    val HandleShape = RoundedCornerShape(2.dp)
}

