package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunPopover(
    contents: List<String>,
    expanded: Boolean,
    onContentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (expanded) {
        Popup {
            Column(
                modifier =
                    modifier
                        .dropShadow(
                            shape = TodakunPopoverDefaults.Shape,
                            shadow =
                                Shadow(
                                    radius = 10.dp,
                                    spread = 1.dp,
                                    color = TodakunColor.black.copy(alpha = 0.08f),
                                ),
                        ).clip(TodakunPopoverDefaults.Shape)
                        .background(TodakunColor.white)
                        .width(TodakunPopoverDefaults.Width)
                        .padding(TodakunPopoverDefaults.OuterContentPadding),
            ) {
                contents.forEach { content ->
                    Text(
                        modifier =
                            Modifier
                                .clickable { onContentClick(content) }
                                .padding(
                                    all = TodakunPopoverDefaults.InnerContentPadding,
                                ),
                        text = content,
                        style = TodakunTypography.body3Medium,
                        color = TodakunColor.gray975,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

object TodakunPopoverDefaults {
    val Shape = RoundedCornerShape(12.dp)
    val OuterContentPadding = 8.dp

    val InnerContentPadding = 12.dp

    val Width = 116.dp
}

@Preview(showBackground = true)
@Composable
private fun TodakunPopoverPreview() {
    TodakunTheme {
        TodakunPopover(listOf("18평"), onContentClick = {}, expanded = true)
    }
}
