package com.kikidan.notification.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kikidan.domain.model.notification.PushNotificationEvent

// TODO: 카카오톡 스타일 상단 드롭다운 배너 UI 구현 (담당자가 별도 작업)
@Composable
fun PushNotificationBanner(
    event: PushNotificationEvent?,
    onDismiss: () -> Unit,
    onClick: (PushNotificationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
}
