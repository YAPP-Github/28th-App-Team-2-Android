package com.kikidan.sajucontents.component

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.button.SecondaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import kotlinx.coroutines.launch

// Day/Year 등 결과 화면 공용 공유 다이얼로그. shareUrl은 화면(도메인)별 경로가 달라(예: day-fortune, year-fortune)
// 호출부가 직접 만들어 전달한다.
@Composable
fun FortuneShareDialog(
    fortuneTitle: String,
    fortuneId: String,
    shareUrl: String,
    onKakaoShareFail: () -> Unit,
    onUrlCopy: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        FortuneShareDialogContent(
            fortuneTitle = fortuneTitle,
            fortuneId = fortuneId,
            shareUrl = shareUrl,
            onKakaoShareFail = onKakaoShareFail,
            onUrlCopy = onUrlCopy,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
private fun FortuneShareDialogContent(
    fortuneTitle: String,
    fortuneId: String,
    shareUrl: String,
    onKakaoShareFail: () -> Unit,
    onUrlCopy: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val shareMessage = stringResource(id = R.string.fortune_share_message, fortuneTitle)
    val kakaoLabel = stringResource(id = R.string.fortune_share_kakao)
    val copyUrlLabel = stringResource(id = R.string.fortune_share_copy_url)
    val cancelLabel = stringResource(id = R.string.fortune_share_cancel)

    Column(
        modifier =
            modifier
                .width(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TodakunColor.white)
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.fortune_share_dialog_title),
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.gray975,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        ShareActionButton(
            text = kakaoLabel,
            containerColor = Color(0xFFFAE301),
            contentColor = TodakunColor.gray925,
            iconRes = R.drawable.ic_login_kakao,
            onClick = {
                val template =
                    TextTemplate(
                        text = shareMessage,
                        link =
                            Link(
                                webUrl = shareUrl,
                                mobileWebUrl = shareUrl,
                                androidExecutionParams = mapOf("id" to fortuneId),
                                iosExecutionParams = mapOf("id" to fortuneId),
                            ),
                    )
                if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
                    ShareClient.instance.shareDefault(context, template) { result, error ->
                        if (error != null || result == null) {
                            onKakaoShareFail()
                        } else {
                            context.startActivity(result.intent)
                        }
                    }
                } else {
                    val webUrl = WebSharerClient.instance.makeDefaultUrl(template)
                    KakaoCustomTabsClient.open(context, webUrl)
                }
            },
        )
        ShareActionButton(
            text = copyUrlLabel,
            containerColor = TodakunColor.coolGray700,
            contentColor = TodakunColor.white,
            iconRes = null,
            onClick = {
                coroutineScope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(shareUrl, shareUrl)))
                    onUrlCopy()
                }
            },
        )
        SecondaryButton(
            text = cancelLabel,
            onClick = onDismiss,
            size = TodakunButtonSize.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ShareActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    iconRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .clickable(
                    onClick = onClick,
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = text,
                style = TodakunTypography.body3SemiBold,
                color = contentColor,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun FortuneShareDialogPreview() {
    TodakunTheme {
        FortuneShareDialogContent(
            fortuneTitle = "이 날짜엔 새로운 시작이 아주 잘 맞아요.",
            fortuneId = "preview-id",
            shareUrl = "https://todakun.com/day-fortune?id=preview-id",
            onKakaoShareFail = {},
            onUrlCopy = {},
            onDismiss = {},
        )
    }
}
