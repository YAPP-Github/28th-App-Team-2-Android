package com.kikidan.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunChatCalenderActionCard(
    category: String,
    dateText: String,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TodakunColor.white)
                .border(1.dp, TodakunColor.coolGray300, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "[$category]",
                style = TodakunTypography.body2SemiBold,
                color = TodakunColor.gray975,
            )
            Text(
                text = dateText,
                style = TodakunTypography.body3Regular,
                color = TodakunColor.gray800,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        CalendarActionButton(
            text = buttonLabel,
            painter = painterResource(id = R.drawable.ic_calendar_add),
            onClick = onButtonClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CalendarActionButton(
    text: String,
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressedOverlay = TodakunColor.gray975.copy(alpha = 0.16f)
    val containerColor =
        if (isPressed) pressedOverlay.compositeOver(TodakunColor.primary600) else TodakunColor.primary600

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = TodakunColor.white,
                ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = TodakunTypography.body3SemiBold,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatCalenderActionCardPreview() {
    TodakunTheme {
        TodakunChatCalenderActionCard(
            category = "계약 · 이사",
            dateText = "2026 . 7 . 25 (토)",
            buttonLabel = "내 캘린더에 추가하기",
            onButtonClick = {},
        )
    }
}
