package com.kikidan.auth.di

import com.kikidan.auth.OAuthTokenProvider
import com.kikidan.auth.google.GoogleOAuthTokenProvider
import com.kikidan.auth.kakao.KakaoOAuthTokenProvider
import com.kikidan.domain.model.auth.OAuthProviderType
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class OAuthModule {
    @Binds
    @IntoMap
    @OAuthProviderKey(OAuthProviderType.KAKAO)
    abstract fun bindKakao(impl: KakaoOAuthTokenProvider): OAuthTokenProvider

    @Binds
    @IntoMap
    @OAuthProviderKey(OAuthProviderType.GOOGLE)
    abstract fun bindGoogle(impl: GoogleOAuthTokenProvider): OAuthTokenProvider
}
