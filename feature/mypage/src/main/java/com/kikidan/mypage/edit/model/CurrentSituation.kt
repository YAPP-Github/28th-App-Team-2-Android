package com.kikidan.mypage.edit.model

import androidx.annotation.StringRes
import com.kikidan.designsystem.R
import com.kikidan.domain.model.user.Job
import com.kikidan.domain.model.user.RelationshipStatus as DomainRelationshipStatus

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

fun LifeStatus.toJob(): Job =
    when (this) {
        LifeStatus.STUDENT -> Job.STUDENT
        LifeStatus.JOB_SEEKING -> Job.JOBSEEKER
        LifeStatus.OFFICE_WORKER -> Job.WORKER
        LifeStatus.SELF_EMPLOYED -> Job.FREELANCER
        LifeStatus.HOMEMAKER -> Job.HOMEMAKER
        LifeStatus.ON_LEAVE_OR_RETIRED -> Job.LEAVER
    }

fun Job.toLifeStatus(): LifeStatus =
    when (this) {
        Job.STUDENT -> LifeStatus.STUDENT
        Job.JOBSEEKER -> LifeStatus.JOB_SEEKING
        Job.WORKER -> LifeStatus.OFFICE_WORKER
        Job.FREELANCER -> LifeStatus.SELF_EMPLOYED
        Job.HOMEMAKER -> LifeStatus.HOMEMAKER
        Job.LEAVER -> LifeStatus.ON_LEAVE_OR_RETIRED
    }

fun RelationshipStatus.toDomain(): DomainRelationshipStatus =
    when (this) {
        RelationshipStatus.SINGLE -> DomainRelationshipStatus.SOLO
        RelationshipStatus.DATING -> DomainRelationshipStatus.DATING
        RelationshipStatus.MARRIED -> DomainRelationshipStatus.MARRY
        RelationshipStatus.DIVORCED -> DomainRelationshipStatus.REMARRY
    }

fun DomainRelationshipStatus.toUiStatus(): RelationshipStatus =
    when (this) {
        DomainRelationshipStatus.SOLO -> RelationshipStatus.SINGLE
        DomainRelationshipStatus.DATING -> RelationshipStatus.DATING
        DomainRelationshipStatus.MARRY -> RelationshipStatus.MARRIED
        DomainRelationshipStatus.REMARRY -> RelationshipStatus.DIVORCED
    }
