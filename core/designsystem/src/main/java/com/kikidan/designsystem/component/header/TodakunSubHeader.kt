package com.kikidan.designsystem.component.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

/**
 * 공통 서브 헤더.
 *
 * 좌측 뒤로가기, 중앙 타이틀, (선택적) 우측 닫기 버튼을 배치하는 Stateless 컴포넌트.
 * 타이틀은 Box 오버레이 배치로 좌우 버튼 폭과 무관하게 항상 중앙 정렬된다.
 *
 * @param title 타이틀 텍스트.
 * @param onBackClick 뒤로가기 클릭 콜백.
 * @param onCloseClick 닫기 클릭 콜백. null이면 닫기 버튼을 표시하지 않는다.
 */
@Composable
fun TodakunSubHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(TodakunTheme.colors.white)
            .padding(horizontal = 20.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = "뒤로 가기",
            tint = TodakunTheme.colors.gray925,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(20.dp)
                .clickable(onClick = onBackClick),
        )

        Text(
            text = title,
            style = TodakunTheme.typography.body2SemiBold,
            color = TodakunTheme.colors.black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center),
        )

        if (onCloseClick != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "닫기",
                tint = TodakunTheme.colors.gray925,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(20.dp)
                    .clickable(onClick = onCloseClick),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunSubHeaderPreview() {
    TodakunTheme {
        TodakunSubHeader(
            title = "타이틀",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunSubHeaderWithCloseButtonPreview() {
    TodakunTheme {
        TodakunSubHeader(
            title = "타이틀",
            onCloseClick = {},
        )
    }
}
