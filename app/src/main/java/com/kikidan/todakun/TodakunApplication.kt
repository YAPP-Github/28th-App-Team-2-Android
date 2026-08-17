package com.kikidan.todakun

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.FirebaseApp
import com.kikidan.auth.OAuthTokenProviderRegistry
import com.kikidan.designsystem.R
import com.kikidan.domain.usecase.RegisterDeviceTokenUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TodakunApplication : Application() {
    @Inject
    lateinit var oAuthTokenProviderRegistry: OAuthTokenProviderRegistry

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        oAuthTokenProviderRegistry.init(this)
        createDefaultNotificationChannel()
    }

    private fun createDefaultNotificationChannel() {
        val channel =
            NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
