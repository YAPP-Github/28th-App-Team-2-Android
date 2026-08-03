package com.kikidan.mypage.home.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
internal fun MyPageHomeHeader(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.mypage_home_title),
        style = TodakunTypography.heading4Bold,
        color = TodakunColor.black,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    )
}
