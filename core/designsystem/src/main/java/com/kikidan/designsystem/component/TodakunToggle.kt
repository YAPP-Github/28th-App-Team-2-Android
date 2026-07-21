package com.kikidan.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TodakunColor

    val trackColor =
        when {
            !enabled -> colors.gray200
            checked -> colors.primary700
            else -> colors.gray200
        }
    val thumbColor =
        when {
            !enabled -> colors.gray100
            else -> colors.white
        }
    val thumbOffsetX: Dp by animateDpAsState(
        targetValue = if (checked) 25.dp else 4.dp,
    )

    Box(
        modifier =
            modifier
                .size(width = 53.dp, height = 30.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(trackColor)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .offset(x = thumbOffsetX)
                    .clip(CircleShape)
                    .background(thumbColor),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunToggleOnPreview() {
    TodakunTheme {
        TodakunToggle(
            checked = true,
            onCheckedChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunToggleOffPreview() {
    TodakunTheme {
        TodakunToggle(
            checked = false,
            onCheckedChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunToggleDisabledPreview() {
    TodakunTheme {
        TodakunToggle(
            checked = false,
            onCheckedChange = {},
            enabled = false,
        )
    }
}
