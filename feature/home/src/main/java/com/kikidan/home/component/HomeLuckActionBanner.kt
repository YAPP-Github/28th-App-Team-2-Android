package com.kikidan.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
internal fun HomeLuckActionBanner(
    onNavigateToLuckAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(TodakunColor.primary500, TodakunColor.primary700),
                    ),
                ).clickable(onClick = onNavigateToLuckAction)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_luck_action_banner_title),
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.white,
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_small_right),
            contentDescription = null,
            tint = TodakunColor.white,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview
@Composable
private fun HomeLuckActionBannerPreview() {
    TodakunTheme { HomeLuckActionBanner(onNavigateToLuckAction = {}) }
}
