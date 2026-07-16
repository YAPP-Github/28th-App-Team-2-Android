package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
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
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxLines:Int = 5,
    placeholder: String = stringResource(R.string.place_holder_chat_input),
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography
    val isFilled = value.isNotBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = TodakunChatInputFieldDefaults.InputFieldRound,
                shadow = Shadow(
                    radius = 20.dp,
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    color = colors.black.copy(alpha = 0.06f)
                )
            )
            .border(
                width = 1.dp,
                color = colors.gray50,
                shape = TodakunChatInputFieldDefaults.InputFieldRound
            )
            .background(color = colors.white, shape = TodakunChatInputFieldDefaults.InputFieldRound)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = typography.body2Regular,
                    color = colors.gray400,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = typography.body2Regular,
                modifier = Modifier.fillMaxWidth(),
                maxLines = maxLines
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isFilled) colors.primary600 else colors.gray50,
                    shape = CircleShape,
                )
                .clickable(enabled = isFilled, onClick = onSendClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_upward),
                contentDescription = null,
                tint = if (isFilled) colors.white else colors.gray300,
            )
        }
    }
}

private object TodakunChatInputFieldDefaults {
    val InputFieldRound = RoundedCornerShape(100.dp)
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
