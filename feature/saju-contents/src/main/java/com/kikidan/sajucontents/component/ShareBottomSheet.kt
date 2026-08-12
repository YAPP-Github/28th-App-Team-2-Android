package com.kikidan.sajucontents.component

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.SecondaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.sajucontents.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    shareUrl: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TodakunColor.white,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.year_fortune_share_title),
                style = TodakunTypography.heading3Bold,
                color = TodakunColor.gray975,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            // ponytail: 카카오톡 공유 SDK(v2-share)가 미설치 상태(설계 문서 4절)라 버튼을 비활성화해 둔다.
            // SDK 도입 시 enabled = true로 바꾸고 실제 공유 인텐트로 교체.
            SecondaryButton(
                text = stringResource(id = R.string.year_fortune_share_kakao),
                onClick = {},
                enabled = false,
                size = TodakunButtonSize.Large,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
            // ponytail: 공유용 웹 랜딩 페이지가 아직 없어(설계 문서 4절) 버튼을 비활성화해 둔다.
            // 랜딩 페이지가 정해지면 enabled = true로 바꾼다.
            PrimaryButton(
                text = stringResource(id = R.string.year_fortune_share_copy_url),
                onClick = {
                    coroutineScope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(shareUrl, shareUrl)))
                    }
                    onDismissRequest()
                },
                enabled = false,
                size = TodakunButtonSize.Large,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
            Text(
                text = stringResource(id = R.string.year_fortune_share_cancel),
                style = TodakunTypography.body2Medium,
                color = TodakunColor.gray700,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDismissRequest)
                        .padding(vertical = 8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareBottomSheetPreview() {
    TodakunTheme {
        ShareBottomSheet(
            shareUrl = "https://todakun.com/year-fortune/2026",
            onDismissRequest = {},
        )
    }
}
