package com.kikidan.todakun

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.domain.model.notification.PushNotificationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val deepLinkEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLinkIntent(intent)
        setContent {
            TodakunTheme {
                TodakunApp(deepLinkEvents = deepLinkEvents)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    /** FCM 백그라운드 알림의 PendingIntent extra를 우선 확인하고, 없으면 외부 공유 링크(intent.data)를 확인한다. */
    private fun handleDeepLinkIntent(intent: Intent?) {
        val deepLink =
            intent?.getStringExtra(PushNotificationEvent.DEEP_LINK_KEY)
                ?: intent?.data?.toString()
        deepLink?.let(deepLinkEvents::tryEmit)
    }
}
