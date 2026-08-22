package com.kikidan.todakun

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.domain.model.notification.PushNotificationEvent
import com.kikidan.navigation.TodakunRoute
import com.kikidan.navigation.parseDeepLink
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodakunTheme {
                TodakunApp(deepLinkRoute = handleDeepLinkIntent(intent))
            }
        }
    }

    /** FCM 백그라운드 알림의 PendingIntent extra를 우선 확인하고, 없으면 외부 공유 링크(intent.data)를 확인한다. */
    private fun handleDeepLinkIntent(intent: Intent?): TodakunRoute? {
        val deepLink = intent?.extras?.getString(PushNotificationEvent.DEEP_LINK_KEY)
        return deepLink?.let { parseDeepLink(it) }
    }
}
