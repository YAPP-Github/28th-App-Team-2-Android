package com.kikidan.sajucontents.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.user.Gender
import com.kikidan.sajucontents.R

/**
 * 성별 선택 UI. Figma에 "선택됨" 상태 시안이 없어 목적 칩과 동일한 규칙(primary500 + white)을
 * 잠정 적용한다 (설계 문서 Q12, 실제 시안 확정 시 교체).
 *
 * ⚠️ 선택값은 State에만 반영되고 서버로 전송되지 않는다 (블로커 B-2).
 */
@Composable
fun GenderSelector(
    selectedGender: Gender?,
    onGenderSelect: (Gender) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GenderOption(
            label = stringResource(id = R.string.date_fortune_gender_male),
            selected = selectedGender == Gender.MALE,
            unselectedBrush = Brush.horizontalGradient(listOf(TodakunColor.sky50, TodakunColor.gray50)),
            onClick = { onGenderSelect(Gender.MALE) },
            modifier = Modifier.weight(1f),
        )
        GenderOption(
            label = stringResource(id = R.string.date_fortune_gender_female),
            selected = selectedGender == Gender.FEMALE,
            unselectedBrush = Brush.horizontalGradient(listOf(TodakunColor.gray50, TodakunColor.primary50)),
            onClick = { onGenderSelect(Gender.FEMALE) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GenderOption(
    label: String,
    selected: Boolean,
    unselectedBrush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush = if (selected) SolidColor(TodakunColor.primary500) else unselectedBrush)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TodakunTypography.body1Medium,
            color = if (selected) TodakunColor.white else TodakunColor.gray800,
        )
    }
}
