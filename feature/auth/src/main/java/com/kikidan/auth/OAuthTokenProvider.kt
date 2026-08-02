package com.kikidan.auth

import android.app.Activity
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OAuthProviderType

interface OAuthTokenProvider {
    val type: OAuthProviderType

    suspend fun authorize(activity: Activity): OAuthCredential

    suspend fun signOut()

    suspend fun unlink()
}
