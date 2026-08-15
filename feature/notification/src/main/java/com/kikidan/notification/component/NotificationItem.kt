package com.kikidan.notification.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunBadge
import com.kikidan.designsystem.component.TodakunBadgeType
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.notification.NotificationType
import com.kikidan.notification.model.NotificationUiModel

@Composable
internal fun NotificationItem(
    notification: NotificationUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TodakunBadge(text = categoryLabel(notification.category), type = TodakunBadgeType.Gray)
                Text(
                    text = notification.relativeTime,
                    style = TodakunTypography.caption1Regular,
                    color = TodakunColor.gray600,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = notification.title,
                style = TodakunTypography.body2Medium,
                color = TodakunColor.black,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = TodakunColor.gray100)
    }
}

@Composable
private fun categoryLabel(type: NotificationType): String =
    stringResource(
        when (type) {
            NotificationType.NOTICE -> R.string.notification_category_notice
            NotificationType.FORTUNE -> R.string.notification_category_fortune
            NotificationType.LUCKY_ACTION -> R.string.notification_category_lucky_action
            NotificationType.AI_COMPLETE -> R.string.notification_category_ai_complete
        },
    )

@Preview(showBackground = true)
@Composable
private fun NotificationItemUnreadPreview() {
    TodakunTheme {
        NotificationItem(
            notification =
                NotificationUiModel(
                    id = "n-1",
                    category = NotificationType.AI_COMPLETE,
                    title = "토닥이 답변이 도착했어요.",
                    relativeTime = "30분 전",
                    isRead = false,
                ),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationItemReadPreview() {
    TodakunTheme {
        NotificationItem(
            notification =
                NotificationUiModel(
                    id = "n-1",
                    category = NotificationType.FORTUNE,
                    title = "택일 결과가 도착했어요.",
                    relativeTime = "2026.06.13",
                    isRead = true,
                ),
            onClick = {},
        )
    }
}
