package com.kikidan.auth.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.auth.R
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.auth.OAuthProviderType

enum class SocialLoginProvider(
    val labelRes: Int,
    val iconRes: Int,
    val containerColor: Color,
    val contentColor: Color,
) {
    KAKAO(
        labelRes = R.string.login_kakao,
        iconRes = R.drawable.ic_login_kakao,
        containerColor = Color(0xFFFAE301),
        contentColor = Color(0xFF000000),
    ),
    GOOGLE(
        labelRes = R.string.login_google,
        iconRes = R.drawable.ic_login_google,
        containerColor = Color(0xFFF1F3F5),
        contentColor = Color(0xFF212121),
    ),
}

fun SocialLoginProvider.toOAuthProviderType(): OAuthProviderType =
    when (this) {
        SocialLoginProvider.KAKAO -> OAuthProviderType.KAKAO
        SocialLoginProvider.GOOGLE -> OAuthProviderType.GOOGLE
    }

@Composable
internal fun SocialLoginButton(
    enabled: Boolean,
    provider: SocialLoginProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(provider.containerColor)
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(id = provider.iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(id = provider.labelRes),
                style = TodakunTypography.body2Medium,
                color = provider.contentColor,
            )
        }
    }
}
