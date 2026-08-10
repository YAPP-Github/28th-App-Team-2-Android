package com.kikidan.onboarding.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunChip
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.onboarding.component.OnboardingLayout
import com.kikidan.onboarding.model.OnboardingStep

@Composable
internal fun ExtraQuestionScreen(
    lifeStage: Job?,
    relationshipStatus: RelationshipStatus?,
    canProceed: Boolean,
    onLifeStageSelect: (Job) -> Unit,
    onRelationshipStatusSelect: (RelationshipStatus) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingLayout(
        progress = OnboardingStep.EXTRA_QUESTION.progress,
        title =
            buildAnnotatedString {
                append(stringResource(R.string.onboarding_extra_title))
            },
        ctaText = stringResource(id = R.string.onboarding_start),
        ctaEnabled = canProceed,
        onCtaClick = onNextClick,
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(52.dp)) {
            ChipQuestion(
                question = stringResource(id = R.string.onboarding_extra_life_stage_question),
                options = Job.entries,
                selected = lifeStage,
                labelResOf = Job::labelRes,
                onSelect = onLifeStageSelect,
            )
            ChipQuestion(
                question = stringResource(id = R.string.onboarding_extra_relationship_question),
                options = RelationshipStatus.entries,
                selected = relationshipStatus,
                labelResOf = RelationshipStatus::labelRes,
                onSelect = onRelationshipStatusSelect,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipQuestion(
    question: String,
    options: List<T>,
    selected: T?,
    labelResOf: (T) -> Int,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = question,
            style = TodakunTypography.body1Bold,
            color = TodakunColor.black,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                TodakunChip(
                    label = stringResource(id = labelResOf(option)),
                    selected = option == selected,
                    onClick = { onSelect(option) },
                )
            }
        }
    }
}

@get:StringRes
private val Job.labelRes: Int
    get() =
        when (this) {
            Job.STUDENT -> R.string.onboarding_extra_life_stage_student
            Job.JOBSEEKER -> R.string.onboarding_extra_life_stage_job_seeker
            Job.WORKER -> R.string.onboarding_extra_life_stage_office_worker
            Job.FREELANCER -> R.string.onboarding_extra_life_stage_self_employed
            Job.HOMEMAKER -> R.string.onboarding_extra_life_stage_homemaker
            Job.LEAVER -> R.string.onboarding_extra_life_stage_on_leave_or_retired
        }

@get:StringRes
private val RelationshipStatus.labelRes: Int
    get() =
        when (this) {
            RelationshipStatus.SOLO -> R.string.onboarding_extra_relationship_single
            RelationshipStatus.DATING -> R.string.onboarding_extra_relationship_in_relationship
            RelationshipStatus.MARRY -> R.string.onboarding_extra_relationship_married
            RelationshipStatus.REMARRY -> R.string.onboarding_extra_relationship_remarried
        }

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ExtraQuestionScreenPreview() {
    TodakunTheme {
        ExtraQuestionScreen(
            lifeStage = Job.STUDENT,
            relationshipStatus = RelationshipStatus.SOLO,
            canProceed = true,
            onLifeStageSelect = {},
            onRelationshipStatusSelect = {},
            onNextClick = {},
            onBackClick = {},
        )
    }
}
