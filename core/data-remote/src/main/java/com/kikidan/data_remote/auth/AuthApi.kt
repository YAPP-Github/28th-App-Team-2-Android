package com.kikidan.data_remote.auth

internal object AuthApi {
    // 선행 슬래시 없음: BASE_URL이 '/'로 끝나므로 병합 시 base path를 유지한다.
    const val REFRESH = "api/v1/auth/refresh"
    const val LOGIN = "api/v1/auth/login"
    const val SIGNUP = "api/v1/auth/signup"
    const val TERMS = "api/v1/terms"

    // R-7 확정: security: []로 인증을 면제한 경로 4개만 포함.
    // LOGOUT은 인증이 필요하므로 포함하지 않는다.
    val NO_AUTH_PATHS: Set<String> = setOf(REFRESH, LOGIN, SIGNUP, TERMS)
}
