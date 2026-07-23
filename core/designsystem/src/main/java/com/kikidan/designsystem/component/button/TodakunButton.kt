package com.kikidan.designsystem.component.button

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography


@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TodakunButton(
        text = text,
        textColor = TodakunColor.white,
        backgroundColor = TodakunColor.primary600,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TodakunButton(
        text = text,
        textColor = TodakunColor.primary700,
        backgroundColor = TodakunColor.primary50,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    )
}


@Composable
private fun TodakunButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean
) {
    val textStyle =
        if (enabled) TodakunTypography.body2SemiBold else TodakunTypography.body2Medium

    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = TodakunColor.gray100,
            disabledContentColor = TodakunColor.gray400
        )
    ) {
        Text(
            text = text,
            style = textStyle
        )
    }
}

@Composable
@Preview
private fun PrimaryButtonPreview() {
    PrimaryButton(
        text = "다음 단계",
        modifier = Modifier
            .width(353.dp)
            .height(52.dp),
        enabled = true,
        onClick = {}
    )
}


@Composable
@Preview
private fun SecondaryButtonPreview() {
    SecondaryButton(
        text = "다음 단계",
        modifier = Modifier
            .width(353.dp)
            .height(52.dp),
        enabled = true,
        onClick = {}
    )
}


@Composable
@Preview
private fun DisableButtonPreview() {
    SecondaryButton(
        text = "다음 단계",
        modifier = Modifier
            .width(353.dp)
            .height(52.dp),
        enabled = false,
        onClick = {}
    )
}
