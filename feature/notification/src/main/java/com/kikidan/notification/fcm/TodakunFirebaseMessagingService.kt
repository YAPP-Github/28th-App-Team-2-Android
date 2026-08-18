package com.kikidan.notification.fcm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kikidan.designsystem.R
import com.kikidan.domain.model.notification.NotificationType
import com.kikidan.domain.model.notification.PushNotificationEvent
import com.kikidan.domain.notification.PushNotificationEventFlow
import com.kikidan.domain.usecase.RegisterDeviceTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 포그라운드면 시스템 알림 대신 인앱 이벤트로 방출하고, 백그라운드/킬드면 직접 시스템 알림을 띄운다.
 */
@AndroidEntryPoint
class TodakunFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var registerDeviceToken: RegisterDeviceTokenUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch { registerDeviceToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val type = data[KEY_TYPE]?.let { runCatching { NotificationType.valueOf(it) }.getOrNull() } ?: return
        val notificationId = data[KEY_NOTIFICATION_ID] ?: return
        val deepLink = data[PushNotificationEvent.DEEP_LINK_KEY]
        val title = message.notification?.title.orEmpty()
        val body = message.notification?.body.orEmpty()

        val isForeground =
            ProcessLifecycleOwner
                .get()
                .lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)

        // 백그라운드의 경우 OnMessageReceived가 아닌 Firebase가 직접 푸시알림 발송 (Notification 필드의 존재)
        if (isForeground) {
            serviceScope.launch {
                PushNotificationEventFlow.emit(
                    PushNotificationEvent(
                        notificationId = notificationId,
                        type = type,
                        title = title,
                        body = body,
                        deepLink = deepLink,
                    ),
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_NOTIFICATION_ID = "notificationId"
    }
}
