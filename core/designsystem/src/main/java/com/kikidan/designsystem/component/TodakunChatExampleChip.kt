package com.kikidan.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunChatExampleChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography

    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = colors.gray400,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            Text(
                modifier = Modifier.padding(all = 2.dp),
                text = text,
                style = typography.body2Regular,
                color = colors.gray800,
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = colors.gray400
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatExampleChipPreview() {
    TodakunTheme {
        TodakunChatExampleChip(
            text = "💼 직장 · 커리어",
            onClick = {},
        )
    }
}
