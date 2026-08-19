package com.kikidan.sajucontents.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.compatibility.CompatibilityOhaeng
import com.kikidan.domain.model.saju.Ohaeng

private val OhaengBarGradient = Brush.horizontalGradient(listOf(TodakunColor.sky300, TodakunColor.primary300))

@Composable
internal fun CompatibilityOhaengBars(
    ohaengs: List<CompatibilityOhaeng>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ohaengs.forEach { entry -> OhaengBarRow(entry = entry) }
    }
}

@Composable
private fun OhaengBarRow(
    entry: CompatibilityOhaeng,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row {
            Text(
                text = "${entry.ohaeng.displayName} ",
                style = TodakunTypography.body3Medium,
                color = TodakunColor.white,
            )
            Text(
                text = entry.ohaeng.hanja,
                style = TodakunTypography.body3Medium,
                color = TodakunColor.whiteOpacity60,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(TodakunColor.whiteOpacity20),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth((entry.percentage / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(99.dp))
                            .background(OhaengBarGradient),
                )
            }
            Text(
                text = "${entry.percentage}%",
                style = TodakunTypography.body3Medium,
                textAlign = TextAlign.End,
                color = TodakunColor.primary200,
                modifier = Modifier.width(40.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00010B)
@Composable
private fun CompatibilityOhaengBarsPreview() {
    TodakunTheme {
        CompatibilityOhaengBars(
            ohaengs =
                listOf(
                    CompatibilityOhaeng(Ohaeng.MOK, 25),
                    CompatibilityOhaeng(Ohaeng.HWA, 30),
                    CompatibilityOhaeng(Ohaeng.TO, 15),
                    CompatibilityOhaeng(Ohaeng.GEUM, 15),
                    CompatibilityOhaeng(Ohaeng.SU, 15),
                ),
        )
    }
}
