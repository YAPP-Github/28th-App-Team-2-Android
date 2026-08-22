package com.kikidan.notification.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.notification.NotificationType
import com.kikidan.domain.model.notification.PushNotificationEvent
import kotlinx.coroutines.delay

private const val ANIMATION_DURATION_MILLIS = 300

@Composable
fun PushNotificationBanner(
    event: PushNotificationEvent?,
    onDismiss: () -> Unit,
    onClick: (PushNotificationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(event) {
        if (event != null) {
            visible = true
            delay(3_000L)
            visible = false
            delay(ANIMATION_DURATION_MILLIS.toLong())
            currentOnDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(ANIMATION_DURATION_MILLIS)) { -it } + fadeIn(),
        exit = slideOutVertically(animationSpec = tween(ANIMATION_DURATION_MILLIS)) { -it } + fadeOut(),
        modifier = modifier,
    ) {
        event?.let {
            PushNotificationBannerContent(
                event = it,
                onClick = { onClick(it) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun PushNotificationBannerContent(
    event: PushNotificationEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TodakunColor.blackOpacity80)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThumbnailImage()
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = event.title,
                style = TodakunTypography.caption1Medium,
                color = TodakunColor.white,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = event.body,
                    style = TodakunTypography.caption2Regular,
                    color = TodakunColor.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                )
                Spacer(Modifier.width(2.dp))
                Box(
                    modifier =
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(TodakunColor.red400),
                )
            }
        }
    }
}

@Composable
private fun ThumbnailImage(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.img_todak_chat_thumbnail),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(40.dp).clip(RoundedCornerShape(12.dp)),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PushNotificationBannerPreview() {
    TodakunTheme {
        PushNotificationBannerContent(
            event =
                PushNotificationEvent(
                    notificationId = "n-1",
                    type = NotificationType.AI_COMPLETE,
                    title = "토닥이",
                    body = "질문에 대한 답변이 도착했어요",
                    deepLink = null,
                ),
            onClick = {},
        )
    }
}
