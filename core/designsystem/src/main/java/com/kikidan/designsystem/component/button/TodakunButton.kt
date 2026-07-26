package com.kikidan.designsystem.component.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography

enum class TodakunButtonIconPosition {
    Start,
    End,
}

enum class TodakunButtonSize(
    val height: Dp,
    val iconSize: Dp,
    val iconSpacing: Dp,
) {
    Small(
        height = 32.dp,
        iconSize = 16.dp,
        iconSpacing = 1.dp,
    ),
    Medium(
        height = 44.dp,
        iconSize = 20.dp,
        iconSpacing = 2.dp,
    ),
    Large(
        height = 52.dp,
        iconSize = 24.dp,
        iconSpacing = 2.dp,
    ),
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    size: TodakunButtonSize,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    painter: Painter? = null,
    contentDescription: String? = null,
    iconPosition: TodakunButtonIconPosition = TodakunButtonIconPosition.Start,
) {
    TodakunButton(
        text = text,
        textColor = TodakunColor.white,
        backgroundColor = TodakunColor.primary600,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        painter = painter,
        contentDescription = contentDescription,
        size = size,
        iconPosition = iconPosition,
    )
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    size: TodakunButtonSize,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    painter: Painter? = null,
    contentDescription: String? = null,
    iconPosition: TodakunButtonIconPosition = TodakunButtonIconPosition.Start,
) {
    TodakunButton(
        text = text,
        textColor = TodakunColor.primary700,
        backgroundColor = TodakunColor.primary50,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        painter = painter,
        contentDescription = contentDescription,
        size = size,
        iconPosition = iconPosition,
    )
}

@Composable
private fun TodakunButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    size: TodakunButtonSize,
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    contentDescription: String? = null,
    iconPosition: TodakunButtonIconPosition = TodakunButtonIconPosition.Start,
) {
    val textStyle =
        if (enabled) TodakunTypography.body2SemiBold else TodakunTypography.body2Medium
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressedOverlay = TodakunColor.gray975.copy(alpha = 0.16f)
    val containerColor =
        if (isPressed) pressedOverlay.compositeOver(backgroundColor) else backgroundColor
    val contentColor =
        if (isPressed) pressedOverlay.compositeOver(textColor) else textColor

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            modifier = Modifier.height(size.height).then(modifier),
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = TodakunColor.gray100,
                    disabledContentColor = TodakunColor.gray400,
                ),
            interactionSource = interactionSource,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(size.iconSpacing, Alignment.CenterHorizontally),
            ) {
                if (painter != null && iconPosition == TodakunButtonIconPosition.Start) {
                    Icon(
                        painter = painter,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(size.iconSize),
                    )
                }
                Text(
                    text = text,
                    style = textStyle,
                )
                if (painter != null && iconPosition == TodakunButtonIconPosition.End) {
                    Icon(
                        painter = painter,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(size.iconSize),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun PrimaryButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TodakunButtonSize.entries.forEach { size ->
            PrimaryButton(
                text = "다음 단계",
                modifier = Modifier.width(353.dp),
                size = size,
                enabled = true,
                onClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun SecondaryButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TodakunButtonSize.entries.forEach { size ->
            SecondaryButton(
                text = "다음 단계",
                modifier = Modifier.width(353.dp),
                size = size,
                enabled = true,
                onClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun DisableButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TodakunButtonSize.entries.forEach { size ->
            SecondaryButton(
                text = "다음 단계",
                modifier = Modifier.width(353.dp),
                size = size,
                enabled = false,
                onClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun PrimaryButtonIconPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TodakunButtonIconPosition.entries.forEach { position ->
            TodakunButtonSize.entries.forEach { size ->
                PrimaryButton(
                    text = "다음 단계",
                    modifier = Modifier.width(353.dp),
                    size = size,
                    enabled = true,
                    painter = ColorPainter(TodakunColor.white), // icon 추가 예정
                    contentDescription = null,
                    iconPosition = position,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
@Preview
private fun SecondaryButtonIconPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TodakunButtonIconPosition.entries.forEach { position ->
            TodakunButtonSize.entries.forEach { size ->
                SecondaryButton(
                    text = "다음 단계",
                    modifier = Modifier.width(353.dp),
                    size = size,
                    enabled = true,
                    painter = ColorPainter(TodakunColor.primary700),
                    contentDescription = null,
                    iconPosition = position,
                    onClick = {},
                )
            }
        }
    }
}
