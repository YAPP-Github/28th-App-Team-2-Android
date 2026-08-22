package com.kikidan.auth.di

import com.kikidan.domain.model.auth.OAuthProviderType
import dagger.MapKey

@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class OAuthProviderKey(
    val value: OAuthProviderType,
)
