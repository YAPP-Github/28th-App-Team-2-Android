package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TodakunColor
    val typography = TodakunTypography

    Box(
        modifier =
            modifier
                .defaultMinSize(minWidth = 60.dp)
                .clip(TodakunTabDefaults.Shape)
                .background(
                    color = if (selected) colors.gray975 else colors.coolGray100,
                ).selectable(
                    selected = selected,
                    role = Role.Tab,
                    onClick = onClick,
                ).padding(
                    vertical = 6.dp,
                    horizontal = 16.dp,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = typography.body2Medium,
            color = if (selected) colors.white else colors.coolGray500,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private object TodakunTabDefaults {
    val Shape = RoundedCornerShape(99.dp)
}

@Preview(showBackground = true)
@Composable
private fun TodakunTabSelectedPreview() {
    TodakunTheme {
        TodakunTab(
            text = "일간",
            selected = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunTabUnselectedPreview() {
    TodakunTheme {
        TodakunTab(
            text = "일간",
            selected = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunTabBothStatesPreview() {
    TodakunTheme {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TodakunTab(text = "선택됨", selected = true, onClick = {})
            TodakunTab(text = "미선택", selected = false, onClick = {})
        }
    }
}
