package com.kikidan.sajucontents.dayfortune.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kikidan.domain.model.dayfortune.FortuneCategoryStar
import kotlinx.collections.immutable.ImmutableList

/**
 * 카테고리 별점 영역. 칸 수는 [categoryStars]의 크기, 별 개수는 각 star 값을 그대로 쓴다(D9).
 * Figma는 3칸 고정 레이아웃이지만 서버가 최대 5개까지 보낼 수 있어 FlowRow로 자동 줄바꿈한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FortuneCategoryStars(
    categoryStars: ImmutableList<FortuneCategoryStar>,
    modifier: Modifier = Modifier,
) {
    if (categoryStars.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categoryStars.forEach { categoryStar ->
            CategoryStarItem(
                categoryStar = categoryStar,
                modifier = Modifier.widthIn(min = 80.dp),
            )
        }
    }
}
