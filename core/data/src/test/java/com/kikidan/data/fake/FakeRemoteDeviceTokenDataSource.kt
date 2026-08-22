package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteDeviceTokenDataSource

class FakeRemoteDeviceTokenDataSource : RemoteDeviceTokenDataSource {
    var lastPostedToken: String? = null
    var throwOnPostDeviceToken: Throwable? = null

    override suspend fun postDeviceToken(token: String) {
        throwOnPostDeviceToken?.let { throw it }
        lastPostedToken = token
    }
}
