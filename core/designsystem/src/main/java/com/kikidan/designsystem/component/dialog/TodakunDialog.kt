package com.kikidan.designsystem.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.SecondaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    description: String? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        TodakunDialogContent(
            title = title,
            confirmText = confirmText,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            modifier = modifier,
            dismissText = dismissText,
            description = description,
        )
    }
}

@Composable
private fun TodakunDialogContent(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    description: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .width(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TodakunColor.white)
                .padding(20.dp),
    ) {
        Text(
            text = title,
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.gray975,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = TodakunTypography.body3Regular,
                color = TodakunColor.gray800,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (dismissText != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SecondaryButton(
                    text = dismissText,
                    onClick = onDismiss,
                    size = TodakunButtonSize.Medium,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = confirmText,
                    onClick = onConfirm,
                    size = TodakunButtonSize.Medium,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            PrimaryButton(
                text = confirmText,
                onClick = onConfirm,
                size = TodakunButtonSize.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TodakunDialogWithDescriptionSingleButtonPreview() {
    TodakunDialogContent(
        title = "타이틀을 입력해주세요",
        description = "본문 내용을 입력해주세요.",
        confirmText = "Label",
        onConfirm = {},
        onDismiss = {},
    )
}

@Composable
@Preview(showBackground = true)
private fun TodakunDialogWithDescriptionTwoButtonsPreview() {
    TodakunDialogContent(
        title = "타이틀을 입력해주세요",
        description = "본문 내용을 입력해주세요.",
        confirmText = "Label",
        dismissText = "Label",
        onConfirm = {},
        onDismiss = {},
    )
}

@Composable
@Preview(showBackground = true)
private fun TodakunDialogSingleButtonPreview() {
    TodakunDialogContent(
        title = "타이틀을 입력해주세요",
        confirmText = "Label",
        onConfirm = {},
        onDismiss = {},
    )
}

@Composable
@Preview(showBackground = true)
private fun TodakunDialogTwoButtonsPreview() {
    TodakunDialogContent(
        title = "타이틀을 입력해주세요",
        confirmText = "Label",
        dismissText = "Label",
        onConfirm = {},
        onDismiss = {},
    )
}
