package com.kikidan.notification.fcm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
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
        } else {
            // 프로세스가 살아있는 백그라운드에서는 직접 시스템 알림을 만들어 딥링크를 PendingIntent에 싣는다.
            // 프로세스가 완전히 종료된 상태에서는 이 콜백 자체가 호출되지 않아 커버 범위 밖이다.
            showSystemNotification(notificationId, title, body, deepLink)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showSystemNotification(
        notificationId: String,
        title: String,
        body: String,
        deepLink: String?,
    ) {
        val contentIntent =
            deepLink?.let { link ->
                Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
        val pendingIntent =
            contentIntent?.let {
                PendingIntent.getActivity(
                    this,
                    notificationId.hashCode(),
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

        val notification =
            NotificationCompat
                .Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .apply { pendingIntent?.let(::setContentIntent) }
                .build()

        NotificationManagerCompat.from(this).notify(notificationId.hashCode(), notification)
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
