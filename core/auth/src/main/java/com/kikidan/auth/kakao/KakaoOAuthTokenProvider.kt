package com.kikidan.auth.kakao

import android.app.Activity
import com.kakao.sdk.user.UserApiClient
import com.kikidan.auth.OAuthException
import com.kikidan.auth.OAuthTokenProvider
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OAuthProviderType
import com.kikidan.domain.model.auth.OAuthToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class KakaoOAuthTokenProvider
    @Inject
    constructor() : OAuthTokenProvider {
        override val type: OAuthProviderType = OAuthProviderType.KAKAO

        override suspend fun authorize(activity: Activity): OAuthCredential {
            val accessToken =
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                    try {
                        loginWithKakaoTalk(activity)
                    } catch (e: OAuthException.Cancelled) {
                        // 사용자가 직접 취소 → 카카오계정 폴백 금지
                        throw e
                    } catch (e: CancellationException) {
                        // 코루틴 취소(화면 이탈 등) → 폴백하면 취소했는데 웹 로그인이 뜬다. 반드시 전파.
                        throw e
                    } catch (e: Exception) {
                        // 그 외 실패(앱 미설치 등) → 카카오계정 웹 로그인 폴백
                        loginWithKakaoAccount(activity)
                    }
                } else {
                    loginWithKakaoAccount(activity)
                }
            return OAuthCredential(OAuthProviderType.KAKAO, OAuthToken(accessToken))
        }

        private suspend fun loginWithKakaoTalk(activity: Activity): String =
            suspendCancellableCoroutine { cont ->
                UserApiClient.instance.loginWithKakaoTalk(activity) { kakaoToken, error ->
                    when {
                        error != null -> cont.resumeWithException(error.toOAuthException())
                        kakaoToken != null -> cont.resume(kakaoToken.accessToken)
                        else -> cont.resumeWithException(OAuthException.Unknown(OAuthProviderType.KAKAO, null))
                    }
                }
            }

        private suspend fun loginWithKakaoAccount(activity: Activity): String =
            suspendCancellableCoroutine { cont ->
                UserApiClient.instance.loginWithKakaoAccount(activity) { kakaoToken, error ->
                    when {
                        error != null -> cont.resumeWithException(error.toOAuthException())
                        kakaoToken != null -> cont.resume(kakaoToken.accessToken)
                        else -> cont.resumeWithException(OAuthException.Unknown(OAuthProviderType.KAKAO, null))
                    }
                }
            }

        override suspend fun signOut(): Unit =
            suspendCancellableCoroutine { cont ->
                UserApiClient.instance.logout { _ ->
                    // logout은 에러가 있어도 로컬 세션은 정리됨 — 항상 성공 처리
                    cont.resume(Unit)
                }
            }

        override suspend fun unlink(): Unit =
            suspendCancellableCoroutine { cont ->
                UserApiClient.instance.unlink { error ->
                    if (error != null) {
                        cont.resumeWithException(error.toOAuthException())
                    } else {
                        cont.resume(Unit)
                    }
                }
            }
    }
