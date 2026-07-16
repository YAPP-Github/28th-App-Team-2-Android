package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TodakunTheme.colors

    Box(
        modifier = modifier
            .size(TodakunCheckboxDefaults.Size)
            .clip(TodakunCheckboxDefaults.Shape)
            .background(if (checked) colors.primary600 else colors.white)
            .then(
                if (!checked) {
                    Modifier.border(1.dp, colors.gray300, TodakunCheckboxDefaults.Shape)
                } else {
                    Modifier
                },
            )
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Checkbox,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_line),
                contentDescription = null,
                tint = colors.white,
            )
        }
    }
}

private object TodakunCheckboxDefaults {
    val Size = 20.dp
    val Shape = RoundedCornerShape(6.dp)
}

@Preview(showBackground = true)
@Composable
private fun TodakunCheckboxPreview() {
    TodakunTheme {
        var checkedOn by remember { mutableStateOf(true) }
        var checkedOff by remember { mutableStateOf(false) }

        Row {
            TodakunCheckbox(checked = checkedOn, onCheckedChange = { checkedOn = it })
            Spacer(modifier = Modifier.width(16.dp))
            TodakunCheckbox(checked = checkedOff, onCheckedChange = { checkedOff = it })
        }
    }
}
