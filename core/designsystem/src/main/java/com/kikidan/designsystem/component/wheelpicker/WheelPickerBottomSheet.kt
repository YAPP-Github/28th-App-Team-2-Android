package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
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
        containerColor = Color.Transparent,
        dragHandle = null,
        modifier = modifier,
        content = {
            //상단 그림자 잘림 방지
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = WheelPickerBottomSheetDefaults.HorizontalMargin)
                    .padding(bottom = bottomSpacing)
                    .dropShadow(
                        shape = WheelPickerBottomSheetDefaults.Shape,
                        shadow = Shadow(
                            radius = 20.dp,
                            spread = 0.dp,
                            offset = DpOffset(0.dp, 0.dp),
                            color = colors.black.copy(alpha = 0.05f),
                        )
                    )
                    .background(colors.white, WheelPickerBottomSheetDefaults.Shape)
                    .clip(WheelPickerBottomSheetDefaults.Shape),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DragHandle()
                content()
            }
        }
    )
}

@Composable
private fun DragHandle(
    modifier: Modifier = Modifier
) {
    val colors = LocalTodakunColor.current
    Box(
        modifier = modifier
            .padding(vertical = 16.dp)
            .size(width = 42.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(colors.gray200),
    )
}

private object WheelPickerBottomSheetDefaults {
    val HorizontalMargin: Dp = 20.dp

    val BottomSpacing: Dp = 16.dp
    val Shape = RoundedCornerShape(12.dp)
    val HandleShape = RoundedCornerShape(2.dp)
}

