package com.kikidan.designsystem.component.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunSubHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(TodakunColor.white)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = stringResource(R.string.header_back_content_description),
            tint = TodakunColor.gray925,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
                    .clickable(onClick = onBackClick),
        )

        Text(
            text = title,
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center),
        )

        if (onCloseClick != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(R.string.header_close_content_description),
                tint = TodakunColor.gray925,
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(20.dp)
                        .clickable(onClick = onCloseClick),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunSubHeaderPreview() {
    TodakunTheme {
        TodakunSubHeader(
            title = "타이틀",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunSubHeaderWithCloseButtonPreview() {
    TodakunTheme {
        TodakunSubHeader(
            title = "타이틀",
            onCloseClick = {},
        )
    }
}
