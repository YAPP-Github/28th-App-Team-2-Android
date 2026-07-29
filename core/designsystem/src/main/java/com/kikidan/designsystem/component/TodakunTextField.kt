package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
) {
    var isFocused by remember { mutableStateOf(false) }
    val hasValue = value.isNotEmpty()
    val showClearButton = isFocused && hasValue
    val showBorder = isFocused && !isError

    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
            textStyle = TodakunTypography.body2Medium.copy(color = TodakunColor.gray975),
            singleLine = true,
            cursorBrush = SolidColor(TodakunTextFieldDefaults.CursorColor),
            decorationBox = { innerTextField ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isError) TodakunColor.red50 else TodakunColor.gray25)
                            .then(
                                if (showBorder) {
                                    Modifier.border(1.dp, TodakunColor.gray975, RoundedCornerShape(12.dp))
                                } else {
                                    Modifier
                                },
                            ).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (!hasValue) {
                            Text(
                                text = placeholder,
                                style = TodakunTypography.body2Regular,
                                color = TodakunColor.gray600,
                            )
                        }
                        innerTextField()
                    }
                    if (showClearButton) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_circle_x_fill),
                            contentDescription = "입력 지우기",
                            tint = TodakunColor.gray300,
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .clickable { onValueChange("") }
                                    .padding(6.dp)
                                    .size(20.dp),
                        )
                    }
                }
            },
        )

        if (isError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = TodakunTypography.caption1Regular,
                color = TodakunColor.red500,
            )
        }
    }
}

private object TodakunTextFieldDefaults {
    val CursorColor = Color(0xFF0040FF)
}

@Preview(name = "TextField - Empty", showBackground = true, widthDp = 320)
@Composable
private fun TodakunTextFieldEmptyPreview() {
    var value by remember { mutableStateOf("") }
    TodakunTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = "이름을 입력해주세요",
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(name = "TextField - Filled", showBackground = true, widthDp = 320)
@Composable
private fun TodakunTextFieldFilledPreview() {
    var value by remember { mutableStateOf("토닥운") }
    TodakunTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = "이름을 입력해주세요",
        modifier = Modifier.padding(16.dp),
    )
}

@Preview(name = "TextField - Error", showBackground = true, widthDp = 320)
@Composable
private fun TodakunTextFieldErrorPreview() {
    var value by remember { mutableStateOf("잘못된 값") }
    TodakunTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = "이름을 입력해주세요",
        isError = true,
        errorMessage = "올바른 형식이 아니에요",
        modifier = Modifier.padding(16.dp),
    )
}
