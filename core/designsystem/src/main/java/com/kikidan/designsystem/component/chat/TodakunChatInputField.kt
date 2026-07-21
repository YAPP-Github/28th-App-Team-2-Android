package com.kikidan.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    placeholder: String = stringResource(R.string.todak_chat_place_holder_chat_input),
) {
    val isFilled = value.isNotBlank()

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = TodakunChatInputFieldDefaults.InputFieldShape,
                    shadow =
                        Shadow(
                            radius = 20.dp,
                            offset = DpOffset(x = 0.dp, y = 4.dp),
                            color = TodakunColor.black.copy(alpha = 0.06f),
                        ),
                ).border(
                    width = 1.dp,
                    color = TodakunColor.gray50,
                    shape = TodakunChatInputFieldDefaults.InputFieldShape,
                ).background(color = TodakunColor.white, shape = TodakunChatInputFieldDefaults.InputFieldShape)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = TodakunTypography.body2Regular,
                    color = TodakunColor.gray400,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TodakunTypography.body2Regular,
                modifier = Modifier.fillMaxWidth(),
                maxLines = maxLines,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .background(
                        color = if (isFilled) TodakunColor.primary600 else TodakunColor.gray50,
                        shape = CircleShape,
                    ).clickable(enabled = isFilled, onClick = onSendClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_upward),
                contentDescription = null,
                tint = if (isFilled) TodakunColor.white else TodakunColor.gray300,
            )
        }
    }
}

object TodakunChatInputFieldDefaults {
    val InputFieldShape = RoundedCornerShape(24.dp)
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatInputFieldDefaultPreview() {
    TodakunTheme {
        TodakunChatInputField(
            value = "",
            onValueChange = {},
            onSendClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunChatInputFieldFilledPreview() {
    TodakunTheme {
        TodakunChatInputField(
            value = "오늘 하루 운세는 어떤가요?",
            onValueChange = {},
            onSendClick = {},
        )
    }
}
