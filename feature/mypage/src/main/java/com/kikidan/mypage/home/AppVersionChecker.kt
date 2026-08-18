package com.kikidan.mypage.home

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AppVersionChecker
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun getCurrentVersionName(): String =
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                .orEmpty()

        suspend fun isLatestVersion(): Boolean =
            try {
                val appUpdateManager = AppUpdateManagerFactory.create(context)
                val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
                appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE
            } catch (e: Exception) {
                // Play Store가 없는 기기(에뮬레이터 등)에서도 마이페이지 조회 자체는 실패하면 안 되므로 조회 실패 시 최신 버전으로 간주한다.
                true
            }
    }
