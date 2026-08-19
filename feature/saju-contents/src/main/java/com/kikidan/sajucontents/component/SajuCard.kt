package com.kikidan.sajucontents.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.Ohaeng

@Composable
internal fun SajuCard(
    hanja: String,
    reading: String,
    ohaeng: Ohaeng,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(ohaeng.containerColor())
                .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = hanja,
            style = TodakunTypography.body1Bold,
            color = TodakunColor.gray975,
        )
        Text(
            text = stringResource(R.string.saju_card_label_format, reading, ohaeng.hanja),
            style = TodakunTypography.caption3Regular,
            color = TodakunColor.gray975,
        )
    }
}

private fun Ohaeng.containerColor(): Color =
    when (this) {
        Ohaeng.MOK -> TodakunColor.teal200
        Ohaeng.HWA -> TodakunColor.red200
        Ohaeng.TO -> TodakunColor.orange200
        Ohaeng.GEUM -> TodakunColor.coolGray300
        Ohaeng.SU -> TodakunColor.sky200
    }
