package com.kikidan.designsystem.component.bottomnavigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunTheme

/** [TodakunBottomNavigation]의 레이아웃 상수. 하드코딩 산재를 막기 위해 한곳에 모은다. */
private object TodakunBottomNavigationDefaults {
    val ContainerHeight = 56.dp
    val ContainerCornerRadius = 24.dp
    val ContainerHorizontalPadding = 12.dp
    val ContentTopPadding = 4.dp
    val IconSize = 24.dp
    val LabelSpacing = 4.dp
    val ShadowRadius = 20.dp
    val ShadowOffsetY = (-4).dp
    const val ShadowAlpha = 0.06f
}

/**
 * 앱 하단에 고정되는 4탭 BottomNavigation.
 *
 * Stateless 프레젠테이셔널 컴포넌트로, 선택 상태와 클릭 콜백을 외부에서 주입받는다.
 * 실제 라우팅 연결은 이 컴포넌트의 책임이 아니다.
 */
@Composable
fun TodakunBottomNavigation(
    selectedItem: TodakunNavItem,
    onItemSelected: (TodakunNavItem) -> Unit,
    modifier: Modifier = Modifier,
    items: List<TodakunNavItem> = TodakunNavItem.entries,
) {
    val containerShape = remember {
        RoundedCornerShape(
            topStart = TodakunBottomNavigationDefaults.ContainerCornerRadius,
            topEnd = TodakunBottomNavigationDefaults.ContainerCornerRadius,
        )
    }
    val containerShadow = remember {
        Shadow(
            radius = TodakunBottomNavigationDefaults.ShadowRadius,
            color = Color.Black,
            alpha = TodakunBottomNavigationDefaults.ShadowAlpha,
            offset = DpOffset(0.dp, TodakunBottomNavigationDefaults.ShadowOffsetY),
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TodakunBottomNavigationDefaults.ContainerHeight)
            .dropShadow(shape = containerShape, shadow = containerShadow)
            .clip(containerShape)
            .background(color = TodakunTheme.colors.white, shape = containerShape)
            .padding(horizontal = TodakunBottomNavigationDefaults.ContainerHorizontalPadding)
            .padding(top = TodakunBottomNavigationDefaults.ContentTopPadding),
    ) {
        items.forEach { item ->
            TodakunBottomNavigationTab(
                item = item,
                selected = item == selectedItem,
                onClick = onItemSelected,
            )
        }
    }
}

@Composable
private fun RowScope.TodakunBottomNavigationTab(
    item: TodakunNavItem,
    selected: Boolean,
    onClick: (TodakunNavItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick(item) }
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TodakunBottomNavigationDefaults.LabelSpacing),
    ) {
        Image(
            painter = painterResource(id = if (selected) item.selectedIconRes else item.unselectedIconRes),
            contentDescription = null,
            modifier = Modifier.size(TodakunBottomNavigationDefaults.IconSize),
        )
        Text(
            text = stringResource(id = item.labelRes),
            style = if (selected) TodakunTheme.typography.caption3SemiBold else TodakunTheme.typography.caption3Medium,
            color = if (selected) TodakunTheme.colors.gray975 else TodakunTheme.colors.gray500,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun TodakunBottomNavigationPreview() {
    TodakunTheme {
        TodakunBottomNavigation(
            selectedItem = TodakunNavItem.LUCKY,
            onItemSelected = {},
        )
    }
}
