package com.kikidan.mypage.edit.model

import androidx.annotation.StringRes
import com.kikidan.designsystem.R

enum class LifeStatus(
    @param:StringRes val labelRes: Int,
) {
    STUDENT(R.string.mypage_current_situation_life_student),
    JOB_SEEKING(R.string.mypage_current_situation_life_job_seeking),
    OFFICE_WORKER(R.string.mypage_current_situation_life_office_worker),
    SELF_EMPLOYED(R.string.mypage_current_situation_life_self_employed),
    HOMEMAKER(R.string.mypage_current_situation_life_homemaker),
    ON_LEAVE_OR_RETIRED(R.string.mypage_current_situation_life_on_leave_or_retired),
}

enum class RelationshipStatus(
    @param:StringRes val labelRes: Int,
) {
    SINGLE(R.string.mypage_current_situation_relationship_single),
    DATING(R.string.mypage_current_situation_relationship_dating),
    MARRIED(R.string.mypage_current_situation_relationship_married),
    DIVORCED(R.string.mypage_current_situation_relationship_divorced),
}
