package com.kikidan.notification.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunProgressIndicator
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.notification.NotificationType
import com.kikidan.notification.component.NotificationItem
import com.kikidan.notification.model.NotificationState
import com.kikidan.notification.model.NotificationUiModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun NotificationScreen(
    state: NotificationState,
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TodakunSubHeader(
                title = stringResource(R.string.notification_title),
                onBackClick = onBackClick,
            )

            when (state) {
                is NotificationState.Loading, NotificationState.Failure -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f))
                }

                is NotificationState.Success -> {
                    if (state.notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.notification_empty),
                                style = TodakunTypography.body2Regular,
                                color = TodakunColor.gray500,
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(state.notifications, key = { it.id }) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onClick = { onNotificationClick(notification.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        when (state) {
            is NotificationState.Loading -> TodakunProgressIndicator()
            is NotificationState.Success, NotificationState.Failure -> Unit
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    TodakunTheme {
        NotificationScreen(
            state =
                NotificationState.Success(
                    persistentListOf(
                        NotificationUiModel("n-1", NotificationType.AI_COMPLETE, "토닥이 답변이 도착했어요.", "30분 전", false),
                        NotificationUiModel("n-2", NotificationType.LUCKY_ACTION, "오늘 행운 액션이 열렸어요.", "3시간 전", true),
                        NotificationUiModel("n-3", NotificationType.FORTUNE, "토실이님과의 궁합이 도착했어요.", "2일 전", true),
                    ),
                ),
            onBackClick = {},
            onNotificationClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationScreenEmptyPreview() {
    TodakunTheme {
        NotificationScreen(
            state = NotificationState.Success(persistentListOf()),
            onBackClick = {},
            onNotificationClick = {},
        )
    }
}
