package com.kikidan.designsystem.component.textfield

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
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

/**
 * Figma의 focus 커서 색상(#0040FF)은 현재 [TodakunColor] 토큰에 없어 컴포넌트 내부 상수로 유지한다.
 */
private val TextFieldCursorColor = Color(0xFF0040FF)

@Composable
fun TodakunTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
) {
    val colors = TodakunTheme.colors
    val typography = TodakunTheme.typography

    var isFocused by remember { mutableStateOf(false) }
    val hasValue = value.isNotEmpty()
    val showClearButton = isFocused && hasValue && !isError
    val showBorder = isFocused && !isError

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isError) colors.red50 else colors.gray25)
                .then(
                    if (showBorder) {
                        Modifier.border(1.dp, colors.gray975, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    if (!hasValue) {
                        Text(
                            text = placeholder,
                            style = typography.body2Regular,
                            color = colors.gray600,
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused },
                        textStyle = typography.body2Medium.copy(color = colors.gray975),
                        singleLine = true,
                        cursorBrush = SolidColor(TextFieldCursorColor),
                    )
                }
                if (showClearButton) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_circle_x_fill),
                        contentDescription = null,
                        tint = colors.gray300,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onValueChange("") },
                    )
                }
            }
        }
        if (isError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = typography.caption1Regular,
                color = colors.red500,
            )
        }
    }
}
