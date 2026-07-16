package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunPopover(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TodakunTheme.colors

    Column(
        modifier = modifier
            .shadow(
                elevation = TodakunPopoverDefaults.ShadowElevation,
                shape = TodakunPopoverDefaults.Shape,
                ambientColor = colors.black.copy(alpha = TodakunPopoverDefaults.ShadowAlpha),
                spotColor = colors.black.copy(alpha = TodakunPopoverDefaults.ShadowAlpha),
            )
            .clip(TodakunPopoverDefaults.Shape)
            .background(colors.white)
            .padding(TodakunPopoverDefaults.ContentPadding),
        content = content,
    )
}

private object TodakunPopoverDefaults {
    val Shape = RoundedCornerShape(12.dp)
    val ContentPadding = 8.dp
    val ShadowElevation = 10.dp
    const val ShadowAlpha = 0.08f
}

@Preview(showBackground = true)
@Composable
private fun TodakunPopoverPreview() {
    TodakunTheme {
        TodakunPopover {
            Text(text = "18평", style = TodakunTheme.typography.body3Medium, color = TodakunTheme.colors.gray975)
        }
    }
}
