package com.kikidan.designsystem.component.selectfield

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
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunSelectField(
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    expanded: Boolean = false,
    onClear: (() -> Unit)? = null,
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography

    val hasValue = value != null
    val isFilled = hasValue || expanded
    val showClearButton = !expanded && hasValue && onClear != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.gray25)
            .then(
                if (expanded) {
                    Modifier.border(1.dp, colors.gray975, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = value ?: placeholder,
            style = if (isFilled) typography.body2Medium else typography.body2Regular,
            color = if (isFilled) colors.gray975 else colors.gray600,
            modifier = Modifier.weight(1f),
        )
        if (showClearButton) {
            Icon(
                painter = painterResource(id = R.drawable.ic_circle_x_fill),
                contentDescription = null,
                tint = colors.gray300,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onClear?.invoke() },
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_small_bottom),
            contentDescription = null,
            tint = colors.gray600,
            modifier = Modifier
                .size(20.dp)
                .rotate(if (expanded) 180f else 0f),
        )
    }
}
