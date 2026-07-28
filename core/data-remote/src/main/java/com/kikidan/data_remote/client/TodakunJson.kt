package com.kikidan.data_remote.client

import kotlinx.serialization.json.Json

internal val TodakunJson: Json =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }
