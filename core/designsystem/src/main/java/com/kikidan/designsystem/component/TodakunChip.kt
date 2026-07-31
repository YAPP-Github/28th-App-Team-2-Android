package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
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
fun TodakunChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TodakunColor
    val typography = TodakunTypography

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(100.dp))
                .background(
                    color = if (selected) colors.primary500 else colors.gray25,
                ).selectable(
                    selected = selected,
                    role = Role.Checkbox,
                    onClick = onClick,
                ).padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = if (selected) typography.body2Medium else typography.body2SemiBold,
            color = if (selected) colors.white else colors.gray700,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun TodakunChip2(
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

private object TodakunChipDefaults {
    val Shape = RoundedCornerShape(100.dp)
    val HorizontalPadding = 10.dp
    val VerticalPadding = 5.dp
}

@Preview(showBackground = true)
@Composable
private fun TodakunChip2Preview() {
    TodakunTheme {
        TodakunChip2(text = "24평", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChipUnselectedPreview() {
    TodakunTheme {
        TodakunChip(
            label = "칩",
            selected = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChipSelectedPreview() {
    TodakunTheme {
        TodakunChip(
            label = "칩",
            selected = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChipDisabledPreview() {
    TodakunTheme {
        TodakunChip(
            label = "칩",
            selected = false,
            onClick = {},
        )
    }
}
