package com.kikidan.data_remote.auth

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import javax.inject.Inject

class BearerTokenCacheInvalidator
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : AuthTokenCacheInvalidator {
        override fun invalidate() {
            client.get().authProvider<BearerAuthProvider>()?.clearToken()
        }
    }
