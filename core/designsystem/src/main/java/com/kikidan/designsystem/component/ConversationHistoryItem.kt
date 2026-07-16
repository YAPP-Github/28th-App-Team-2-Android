package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun ConversationHistoryItem(
    title: String,
    relativeTime: String,
    isUnread: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(width = 1.dp, color = colors.gray100)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = typography.body1Medium,
                color = colors.black,
            )
            if (isUnread) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.red400),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = null,
                tint = colors.gray500,
                modifier = Modifier
                    .size(23.dp)
                    .clickable(onClick = onDeleteClick),
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = relativeTime,
            style = typography.body3Regular,
            color = colors.gray600,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationHistoryItemUnreadPreview() {
    TodakunTheme {
        ConversationHistoryItem(
            title = "오늘의 운세",
            relativeTime = "30분 전",
            isUnread = true,
            onClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationHistoryItemReadPreview() {
    TodakunTheme {
        ConversationHistoryItem(
            title = "오늘의 운세",
            relativeTime = "30분 전",
            isUnread = false,
            onClick = {},
            onDeleteClick = {},
        )
    }
}
