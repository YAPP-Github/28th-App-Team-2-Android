package com.kikidan.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary50)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = typography.body2Regular,
            color = colors.coolGray800,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatExampleChipPreview() {
    TodakunTheme {
        TodakunChatExampleChip(
            text = "📅 중요한 일정 잡기 좋은 날인지 궁금해",
            onClick = {},
        )
    }
}
