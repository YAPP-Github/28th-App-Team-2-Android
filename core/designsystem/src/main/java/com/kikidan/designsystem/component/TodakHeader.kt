package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
fun TodakHeader(
    title: String,
    progress: Float,
    freeChatUsed: Int,
    freeChatTotal: Int,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    onChatIconClick: () -> Unit = {},
    onNotesIconClick: () -> Unit = {},
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.white)
            .statusBarsPadding(),
    ) {
        TodakunProgressBar(
            progress = progress,
            showBackButton = true,
            onBackClick = onBackClick,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = typography.body2SemiBold,
                    color = colors.black,
                )
                Text(
                    text = "오늘 무료 채팅 $freeChatUsed/$freeChatTotal",
                    style = typography.body3Regular,
                    color = colors.gray500,
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chat_add),
                contentDescription = null,
                tint = colors.gray975,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onChatIconClick),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_notes),
                contentDescription = null,
                tint = colors.gray975,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
                    .clickable(onClick = onNotesIconClick),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                tint = colors.gray975,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
                    .clickable(onClick = onCloseClick),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.gray50),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakHeaderPreview() {
    TodakunTheme {
        TodakHeader(
            title = "토닥이",
            progress = 0.5f,
            freeChatUsed = 2,
            freeChatTotal = 3,
        )
    }
}
