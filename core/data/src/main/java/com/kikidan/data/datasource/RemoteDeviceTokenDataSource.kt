package com.kikidan.data.datasource

interface RemoteDeviceTokenDataSource {
    suspend fun postDeviceToken(token: String)
}
