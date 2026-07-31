package com.kikidan.todakun

import android.app.Application
import com.kikidan.auth.OAuthTokenProviderRegistry
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TodakunApplication : Application() {
    @Inject
    lateinit var oAuthTokenProviderRegistry: OAuthTokenProviderRegistry

    override fun onCreate() {
        super.onCreate()
        oAuthTokenProviderRegistry.init(this)
    }
}
