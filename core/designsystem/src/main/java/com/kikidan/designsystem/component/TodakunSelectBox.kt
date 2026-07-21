package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun TodakunSelectBox(
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
                .height(48.dp)
                .clip(TodakunSelectBoxDefaults.Shape)
                .background(
                    color = if (selected) colors.primary50 else colors.white,
                ).border(
                    width = 1.dp,
                    color = if (selected) colors.primary600 else colors.coolGray300,
                    shape = TodakunSelectBoxDefaults.Shape,
                ).selectable(
                    selected = selected,
                    role = Role.Tab,
                    onClick = onClick,
                ).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = typography.body1Medium,
            color = if (selected) colors.primary700 else colors.coolGray800,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(136.dp),
        )
    }
}

object TodakunSelectBoxDefaults {
    val Shape = RoundedCornerShape(12.dp)
}

@Preview(showBackground = true)
@Composable
private fun TodakunSelectBoxPreview() {
    TodakunTheme {
        TodakunSelectBox(
            text = "남성",
            selected = true,
            onClick = {},
        )
    }
}
