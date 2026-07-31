package com.kikidan.auth.google

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import com.kikidan.auth.OAuthException
import com.kikidan.domain.model.auth.OAuthProviderType.GOOGLE

internal fun Throwable.toOAuthException(): OAuthException =
    when (this) {
        is GetCredentialCancellationException -> OAuthException.Cancelled(GOOGLE, this)
        is NoCredentialException -> OAuthException.NoCredential(GOOGLE, this)
        is GetCredentialInterruptedException -> OAuthException.Network(GOOGLE, this)
        else -> OAuthException.Unknown(GOOGLE, this)
    }
