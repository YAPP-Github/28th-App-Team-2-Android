package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme

enum class TodakunBadgeType {
    Green,
    Yellow,
    Pink,
    Purple,
    Blue,
    Gray,
}

@Composable
fun TodakunBadge(
    text: String,
    modifier: Modifier = Modifier,
    type: TodakunBadgeType = TodakunBadgeType.Gray,
) {
    val colors = TodakunTheme.colors
    val (backgroundColor, textColor) = type.toColors(colors)

    Row(
        modifier = modifier
            .clip(TodakunBadgeDefaults.Shape)
            .background(backgroundColor)
            .padding(
                horizontal = TodakunBadgeDefaults.HorizontalPadding,
                vertical = TodakunBadgeDefaults.VerticalPadding,
            ),
    ) {
        Text(
            text = text,
            style = TodakunTheme.typography.caption2SemiBold,
            color = textColor,
        )
    }
}

private fun TodakunBadgeType.toColors(colors: TodakunColor): Pair<Color, Color> = when (this) {
    TodakunBadgeType.Green -> colors.teal100 to colors.teal800
    TodakunBadgeType.Yellow -> colors.orange100 to colors.orange800
    TodakunBadgeType.Pink -> colors.pink100 to colors.pink800
    TodakunBadgeType.Purple -> colors.primary100 to colors.primary800
    TodakunBadgeType.Blue -> colors.sky100 to colors.sky800
    TodakunBadgeType.Gray -> colors.coolGray100 to colors.coolGray500
}

private object TodakunBadgeDefaults {
    val Shape = RoundedCornerShape(6.dp)
    val HorizontalPadding = 6.dp
    val VerticalPadding = 3.dp
}

@Preview(showBackground = true)
@Composable
private fun TodakunBadgePreview() {
    TodakunTheme {
        Row {
            TodakunBadge(text = "직장", type = TodakunBadgeType.Green)
            TodakunBadge(text = "건강", type = TodakunBadgeType.Yellow)
            TodakunBadge(text = "연애", type = TodakunBadgeType.Pink)
            TodakunBadge(text = "관계", type = TodakunBadgeType.Purple)
            TodakunBadge(text = "금전", type = TodakunBadgeType.Blue)
            TodakunBadge(text = "category", type = TodakunBadgeType.Gray)
        }
    }
}
