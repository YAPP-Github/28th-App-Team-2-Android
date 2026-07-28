package com.kikidan.auth.google

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kikidan.auth.BuildConfig
import com.kikidan.auth.OAuthCredential
import com.kikidan.auth.OAuthException
import com.kikidan.auth.OAuthToken
import com.kikidan.auth.OAuthTokenProvider
import com.kikidan.domain.model.auth.OAuthProviderType
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class GoogleOAuthTokenProvider
    @Inject
    constructor(
        private val credentialManager: CredentialManager,
    ) : OAuthTokenProvider {
        override val type: OAuthProviderType = OAuthProviderType.GOOGLE

        override suspend fun authorize(activity: Activity): OAuthCredential {
            val option =
                GetGoogleIdOption
                    .Builder()
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(option)
                    .build()
            val idToken =
                try {
                    val result = credentialManager.getCredential(activity, request)
                    val credential = result.credential
                    if (credential !is CustomCredential ||
                        credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        throw OAuthException.Unknown(
                            OAuthProviderType.GOOGLE,
                            IllegalStateException("Unexpected credential type: ${credential.type}"),
                        )
                    }
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                } catch (e: CancellationException) {
                    throw e
                } catch (e: OAuthException) {
                    throw e
                } catch (e: Exception) {
                    throw e.toOAuthException()
                }
            return OAuthCredential(OAuthProviderType.GOOGLE, OAuthToken(idToken))
        }

        override suspend fun signOut() {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
                // Best-effort 로컬 자격증명 상태 초기화 — 실패해도 진행
            }
        }

        // Credential Manager에는 revoke API가 없어 로컬 상태 초기화만 수행한다.
        // 실제 Google 연결 해제는 백엔드가 처리
        override suspend fun unlink() {
            signOut()
        }
    }
