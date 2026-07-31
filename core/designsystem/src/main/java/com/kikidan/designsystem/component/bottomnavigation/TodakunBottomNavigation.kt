package com.kikidan.designsystem.component.bottomnavigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunBottomNavigation(
    selectedItem: TodakunNavItem,
    onItemSelect: (TodakunNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(TodakunBottomNavigationDefaults.ContainerHeight)
                .dropShadow(
                    shape = TodakunBottomNavigationDefaults.ContainerShape,
                    shadow =
                        Shadow(
                            radius = TodakunBottomNavigationDefaults.ShadowRadius,
                            color = TodakunColor.black.copy(alpha = TodakunBottomNavigationDefaults.SHADOW_ALPHA),
                            offset = DpOffset(0.dp, TodakunBottomNavigationDefaults.ShadowOffsetY),
                        ),
                ).clip(TodakunBottomNavigationDefaults.ContainerShape)
                .background(color = TodakunColor.white, shape = TodakunBottomNavigationDefaults.ContainerShape)
                .padding(horizontal = TodakunBottomNavigationDefaults.ContainerHorizontalPadding)
                .padding(top = TodakunBottomNavigationDefaults.ContentTopPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TodakunNavItem.entries.forEach { item ->
            TodakunBottomNavigationTab(
                item = item,
                selected = item == selectedItem,
                onClick = onItemSelect,
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
        modifier =
            Modifier
                .weight(1f)
                .clickable(
                    onClick = { onClick(item) },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ),
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
            style = if (selected) TodakunTypography.caption3SemiBold else TodakunTypography.caption3Medium,
            color = if (selected) TodakunColor.gray975 else TodakunColor.gray500,
        )
    }
}

private object TodakunBottomNavigationDefaults {
    val ContainerHeight = 56.dp
    val ContainerCornerRadius = 24.dp

    val ContainerShape =
        RoundedCornerShape(
            topStart = ContainerCornerRadius,
            topEnd = ContainerCornerRadius,
        )
    val ContainerHorizontalPadding = 12.dp
    val ContentTopPadding = 4.dp
    val IconSize = 24.dp
    val LabelSpacing = 4.dp
    val ShadowRadius = 20.dp
    val ShadowOffsetY = (-4).dp
    const val SHADOW_ALPHA = 0.06f
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun TodakunBottomNavigationPreview() {
    TodakunTheme {
        TodakunBottomNavigation(
            selectedItem = TodakunNavItem.FORTUNE_TELLING,
            onItemSelect = {},
        )
    }
}
