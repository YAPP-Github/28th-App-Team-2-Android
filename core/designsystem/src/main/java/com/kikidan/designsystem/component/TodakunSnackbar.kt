package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunSnackbar(
    text: String,
    modifier: Modifier = Modifier,
    onDismissClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .height(36.dp)
                .clip(SnackbarShadowDefaults.SnackbarShape)
                .background(color = TodakunColor.blackOpacity80)
                .padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = TodakunTypography.body3Regular,
            color = TodakunColor.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onDismissClick != null) {
            Spacer(modifier = Modifier.width(SnackbarShadowDefaults.ContentGap))
            SnackbarDismissButton(
                onClick = onDismissClick,
                tint = TodakunColor.gray50,
            )
        }
    }
}

@Composable
fun TodakunLuckySnackbar(
    text: String,
    modifier: Modifier = Modifier,
    onDismissClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .dropShadow(
                    shape = SnackbarShadowDefaults.SnackbarShape,
                    shadow =
                        Shadow(
                            radius = 20.dp,
                            color = SnackbarShadowDefaults.ShadowColor,
                        ),
                ).height(44.dp)
                .clip(SnackbarShadowDefaults.SnackbarShape)
                .background(
                    brush =
                        Brush.horizontalGradient(
                            0.0f to TodakunColor.primary600,
                            0.5f to TodakunColor.primary800,
                            1.0f to TodakunColor.sky600,
                        ),
                ).padding(start = 18.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = TodakunTypography.body3Regular,
            color = TodakunColor.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onDismissClick != null) {
            Spacer(modifier = Modifier.width(SnackbarShadowDefaults.ContentGap))
            SnackbarDismissButton(
                onClick = onDismissClick,
                tint = TodakunColor.whiteOpacity60,
            )
        }
    }
}

@Composable
private fun SnackbarDismissButton(
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(SnackbarShadowDefaults.DismissButtonSize),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = stringResource(id = R.string.snackbar_dismiss),
            tint = tint,
            modifier = Modifier.size(SnackbarShadowDefaults.DismissIconSize),
        )
    }
}

private object SnackbarShadowDefaults {
    /** `box-shadow: 0 0 20px rgba(156, 138, 246, 0.5)` */
    val ShadowColor = Color(0x809C8AF6)

    val SnackbarShape = RoundedCornerShape(8.dp)

    val ContentGap = 8.dp

    val DismissButtonSize = 20.dp

    val DismissIconSize = 16.dp
}

@Preview(showBackground = true)
@Composable
private fun TodakunSnackbarPreview() {
    TodakunTheme {
        Box(modifier = Modifier.padding(32.dp)) {
            TodakunSnackbar(text = "메시지")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunSnackbarWithDismissPreview() {
    TodakunTheme {
        Box(modifier = Modifier.padding(32.dp)) {
            TodakunSnackbar(
                text = "메시지에 마침표를 찍어주세요.",
                onDismissClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunLuckySnackbarPreview() {
    TodakunTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
            TodakunLuckySnackbar(
                text = "운세 메시지",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunLuckySnackbarWithDismissPreview() {
    TodakunTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
            TodakunLuckySnackbar(
                text = "오늘의 운세가 도착했습니다",
                onDismissClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
