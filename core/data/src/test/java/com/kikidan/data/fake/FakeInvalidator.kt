package com.kikidan.data.fake

import com.kikidan.data.auth.AuthTokenCacheInvalidator

class FakeInvalidator : AuthTokenCacheInvalidator {
    var invalidateCalled = false

    override fun invalidate() {
        invalidateCalled = true
    }
}
