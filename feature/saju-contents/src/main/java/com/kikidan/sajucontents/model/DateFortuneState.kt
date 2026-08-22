package com.kikidan.sajucontents.model

import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.user.Gender
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

data class DateFortuneState(
    val selectedPurpose: DayFortunePurpose? = null,
    // ⚠️ 서버 Request에 gender 필드가 없다(블로커 B-2). UseCase 호출에는 전달하지 않는다.
    val selectedGender: Gender? = null,
    val selectedDates: ImmutableList<LocalDate> = persistentListOf(),
    val isSheetVisible: Boolean = false,
    val submitState: DateFortuneSubmitState? = null,
) {
    // selectedGender는 서버로 보내지 않는 값이라 필수 조건에서 제외한다(B-2 회귀 방지).
    val canSubmit: Boolean
        get() = selectedPurpose != null && selectedDates.isNotEmpty() && submitState !is DateFortuneSubmitState.Loading
}

sealed interface DateFortuneSubmitState {
    data object Loading : DateFortuneSubmitState

    data object Success : DateFortuneSubmitState

    data object Failure : DateFortuneSubmitState
}
