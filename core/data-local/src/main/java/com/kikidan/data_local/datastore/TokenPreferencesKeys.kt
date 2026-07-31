package com.kikidan.data_local.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

internal object TokenPreferencesKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
}
