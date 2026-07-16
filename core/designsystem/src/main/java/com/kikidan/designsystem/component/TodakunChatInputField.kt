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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "토닥이에게 운세 물어보기",
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography
    val isFilled = value.isNotBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.gray50, shape = RoundedCornerShape(100))
            .background(color = colors.white, shape = RoundedCornerShape(100))
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
                textStyle = TextStyle(
                    color = colors.black,
                    fontSize = typography.body2Regular.fontSize,
                    fontWeight = typography.body2Regular.fontWeight,
                    fontFamily = typography.body2Regular.fontFamily,
                    lineHeight = typography.body2Regular.lineHeight,
                ),
                modifier = Modifier.fillMaxWidth(),
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
