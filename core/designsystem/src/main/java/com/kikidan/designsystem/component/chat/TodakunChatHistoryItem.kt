package com.kikidan.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
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
import com.kikidan.designsystem.theme.LocalTodakunColor
import com.kikidan.designsystem.theme.LocalTodakunTypography
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunChatHistoryItem(
    title: String,
    relativeTime: String,
    isUnread: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TodakunTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                top = 20.dp
            ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
            )
        ) {
            TodakChatHistoryContent(
                title = title,
                relativeTime = relativeTime,
                isUnread = isUnread,
                onDeleteClick = onDeleteClick
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = colors.gray100
        )
    }
}

@Composable
private fun ColumnScope.TodakChatHistoryContent(
    title: String,
    relativeTime: String,
    isUnread: Boolean,
    onDeleteClick:()-> Unit,
    modifier:Modifier = Modifier
) {
    val typography = LocalTodakunTypography.current
    val colors = LocalTodakunColor.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
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

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = relativeTime,
        style = typography.body3Regular,
        color = colors.gray600,
    )

    Spacer(modifier = Modifier.height(20.dp))
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatHistoryItemUnreadPreview() {
    TodakunTheme {
        TodakunChatHistoryItem(
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
private fun TodakunChatHistoryItemReadPreview() {
    TodakunTheme {
        TodakunChatHistoryItem(
            title = "오늘의 운세",
            relativeTime = "30분 전",
            isUnread = false,
            onClick = {},
            onDeleteClick = {},
        )
    }
}
