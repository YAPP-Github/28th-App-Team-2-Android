package com.kikidan.data.auth

// Ktor Auth 플러그인의 인메모리 토큰 캐시를 무효화하는 훅.
// 앱이 로그인 상태 없이 콜드 스타트한 뒤 로그인하면, AuthTokenHolder가 null을 캐시하고 있어
// DataStore에 토큰을 저장해도 캐시된 null이 유지된다. saveToken/clearToken 후 이 훅을 호출해
// BearerAuthProvider.clearToken()으로 캐시를 비운다.
interface AuthTokenCacheInvalidator {
    fun invalidate()
}
