package com.kikidan.data_local.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.data_local.datastore.TokenPreferencesKeys
import com.kikidan.data_local.di.AuthTokenDataStore
import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalTokenDataSourceImpl
    @Inject
    constructor(
        @param:AuthTokenDataStore private val dataStore: DataStore<Preferences>,
    ) : LocalTokenDataSource {
        override fun observeToken(): Flow<AuthToken?> = dataStore.data.map { prefs -> prefs.toAuthToken() }

        override suspend fun getToken(): AuthToken? = dataStore.data.first().toAuthToken()

        override suspend fun saveToken(token: AuthToken) {
            dataStore.edit { prefs ->
                prefs[TokenPreferencesKeys.ACCESS_TOKEN] = token.accessToken
                prefs[TokenPreferencesKeys.REFRESH_TOKEN] = token.refreshToken
            }
        }

        override suspend fun clearToken() {
            dataStore.edit { prefs -> prefs.clear() }
        }

        private fun Preferences.toAuthToken(): AuthToken? {
            val access = this[TokenPreferencesKeys.ACCESS_TOKEN]
            val refresh = this[TokenPreferencesKeys.REFRESH_TOKEN]
            return if (access != null && refresh != null) AuthToken(access, refresh) else null
        }
    }
