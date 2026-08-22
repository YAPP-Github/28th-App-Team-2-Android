package com.kikidan.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunChatHeader(
    title: String,
    freeChatRemaining: Int,
    freeChatTotal: Int,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    onChatIconClick: () -> Unit = {},
    onNotesIconClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(TodakunColor.white)
                .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = null,
            tint = TodakunColor.gray975,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
                    .clickable(
                        onClick = onCloseClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ),
        )

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = TodakunTypography.body2SemiBold,
                color = TodakunColor.black,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.todak_chat_today_free_amount))
                    withStyle(SpanStyle(color = TodakunColor.gray800)) {
                        append(freeChatRemaining.toString())
                    }
                    append("/$freeChatTotal")
                },
                style = TodakunTypography.body3Medium,
                color = TodakunColor.gray500,
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chat_add),
                contentDescription = null,
                tint = TodakunColor.gray975,
                modifier =
                    Modifier
                        .size(20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onChatIconClick,
                        ),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_notes),
                contentDescription = null,
                tint = TodakunColor.gray975,
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .size(20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onNotesIconClick,
                        ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatHeaderPreview() {
    TodakunTheme {
        TodakunChatHeader(
            title = "토닥이",
            freeChatRemaining = 2,
            freeChatTotal = 3,
        )
    }
}
