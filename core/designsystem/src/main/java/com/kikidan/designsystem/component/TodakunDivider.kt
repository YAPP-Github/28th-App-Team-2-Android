package com.kikidan.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme

enum class TodakunDividerType {
    Line,
    Section,
}

@Composable
fun TodakunDivider(
    modifier: Modifier = Modifier,
    type: TodakunDividerType = TodakunDividerType.Line,
) {
    val (thickness, color) =
        when (type) {
            TodakunDividerType.Line -> TodakunDividerDefaults.LineThickness to TodakunColor.gray100
            TodakunDividerType.Section -> TodakunDividerDefaults.SectionThickness to TodakunColor.gray25
        }

    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = thickness,
        color = color,
    )
}

object TodakunDividerDefaults {
    val LineThickness = 1.dp
    val SectionThickness = 10.dp
}

@Preview(showBackground = true)
@Composable
private fun TodakunDividerPreview() {
    TodakunTheme {
        Column {
            TodakunDivider(type = TodakunDividerType.Line)
            Spacer(modifier = Modifier.height(16.dp))
            TodakunDivider(type = TodakunDividerType.Section)
        }
    }
}
