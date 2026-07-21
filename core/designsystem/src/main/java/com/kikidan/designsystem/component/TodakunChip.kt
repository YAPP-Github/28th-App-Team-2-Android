package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .clip(TodakunChipDefaults.Shape)
                .background(TodakunColor.primary700)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ).padding(
                    horizontal = TodakunChipDefaults.HorizontalPadding,
                    vertical = TodakunChipDefaults.VerticalPadding,
                ),
    ) {
        Text(
            text = text,
            style = TodakunTypography.body3Medium,
            color = TodakunColor.white,
        )
    }
}

object TodakunChipDefaults {
    val Shape = RoundedCornerShape(100.dp)
    val HorizontalPadding = 10.dp
    val VerticalPadding = 5.dp
}

@Preview(showBackground = true)
@Composable
private fun TodakunChipPreview() {
    TodakunTheme {
        TodakunChip(text = "24평", onClick = {})
    }
}
