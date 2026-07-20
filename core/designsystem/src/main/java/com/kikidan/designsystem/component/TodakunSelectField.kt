package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunSelectField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    expanded: Boolean = false,
    onClear: () -> Unit = {},
) {
    val isFilled = value.isNotEmpty() || expanded
    val showClearButton = !expanded && value.isNotEmpty()

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TodakunColor.gray25)
                .then(
                    if (expanded) {
                        Modifier.border(1.dp, TodakunColor.gray975, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    },
                ).clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = value.ifEmpty { if (expanded) "" else placeholder },
            style = if (isFilled) TodakunTypography.body2Medium else TodakunTypography.body2Regular,
            color = if (isFilled) TodakunColor.gray975 else TodakunColor.gray600,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        if (showClearButton) {
            Icon(
                painter = painterResource(id = R.drawable.ic_circle_x_fill),
                contentDescription = null,
                tint = TodakunColor.gray300,
                modifier =
                    Modifier
                        .size(20.dp)
                        .clickable(onClick = onClear),
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_small_bottom),
            contentDescription = null,
            tint = TodakunColor.gray600,
            modifier =
                Modifier
                    .size(20.dp)
                    .rotate(if (expanded) 180f else 0f),
        )
    }
}

@Preview(name = "SelectField - Placeholder", showBackground = true, widthDp = 320)
@Composable
private fun TodakunSelectFieldPlaceholderPreview() {
    TodakunSelectField(
        value = "",
        onClick = {},
        placeholder = "성별을 선택해주세요",
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(name = "SelectField - Selected", showBackground = true, widthDp = 320)
@Composable
private fun TodakunSelectFieldSelectedPreview() {
    TodakunSelectField(
        value = "여성",
        onClick = {},
        placeholder = "성별을 선택해주세요",
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(name = "SelectField - Expanded", showBackground = true, widthDp = 320)
@Composable
private fun TodakunSelectFieldExpandedPreview() {
    TodakunSelectField(
        value = "여성",
        onClick = {},
        placeholder = "성별을 선택해주세요",
        expanded = true,
        modifier = Modifier.padding(16.dp),
    )
}
