package com.kikidan.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunChatUserInputBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp
                )
            )
            .background(colors.gray50)
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            style = typography.body2Medium,
            color = colors.black,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatUserInputBubblePreview() {
    TodakunTheme {
        TodakunChatUserInputBubble(text = "오늘 하루 운세는 어떤가요?")
    }
}
