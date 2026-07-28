package com.kikidan.data_remote.mapper

import com.kikidan.data_remote.dto.auth.RefreshResponse
import com.kikidan.domain.model.auth.AuthToken

internal fun RefreshResponse.toDomain(): AuthToken =
    AuthToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
