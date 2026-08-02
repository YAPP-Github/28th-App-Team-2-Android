package com.kikidan.onboarding.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunCheckbox
import com.kikidan.designsystem.component.TodakunDivider
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.onboarding.OnboardingTerm
import com.kikidan.onboarding.R
import com.kikidan.onboarding.component.OnboardingScaffold
import com.kikidan.onboarding.model.OnboardingStep
import com.kikidan.onboarding.model.TermsAgreementUiModel

@Composable
internal fun TermsScreen(
    termsAgreement: TermsAgreementUiModel,
    canProceed: Boolean,
    onTermChange: (OnboardingTerm) -> Unit,
    onAllTermsChange: (Boolean) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingScaffold(
        progress = OnboardingStep.TERMS.progress,
        progressLabel = stringResource(id = R.string.onboarding_terms_progress_label),
        title =
            buildAnnotatedString {
                append(stringResource(id = R.string.onboarding_terms_title))
            },
        ctaText = stringResource(id = R.string.onboarding_start),
        ctaEnabled = canProceed,
        onCtaClick = onNextClick,
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OnboardingTerm.entries.forEach { term ->
                TermRow(
                    label = term.toResourceString(),
                    checked = termsAgreement.isAgreed(term),
                    textStyleEmphasized = false,
                    onCheckedChange = { onTermChange(term) },
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        TodakunDivider(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))

        TermRow(
            label = stringResource(id = R.string.onboarding_terms_agree_all),
            checked = termsAgreement.allSelected,
            textStyleEmphasized = true,
            onCheckedChange = onAllTermsChange,
        )
    }
}

@Composable
private fun TermRow(
    label: String,
    checked: Boolean,
    textStyleEmphasized: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onMoreDetailClick: () -> Unit = { /* TODO 외부 링크 열기  */ },
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Checkbox,
                    interactionSource = null,
                    indication = null,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TodakunCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = if (textStyleEmphasized) TodakunTypography.body2SemiBold else TodakunTypography.body3Regular,
            color = TodakunColor.black,
        )
        Icon(
            modifier =
                Modifier
                    .padding(horizontal = 7.dp)
                    .clickable(
                        onClick = onMoreDetailClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ),
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = TodakunColor.gray300,
        )
    }
}

@Composable
private fun OnboardingTerm.toResourceString(): String =
    when (this) {
        OnboardingTerm.SERVICE -> stringResource(R.string.onboarding_terms_service)
        OnboardingTerm.MARKETING -> stringResource(R.string.onboarding_terms_marketing)
        OnboardingTerm.AI_DATA_TRANSFER -> stringResource(R.string.onboarding_terms_ai_data_transfer)
        OnboardingTerm.PRIVACY -> stringResource(R.string.onboarding_terms_privacy)
    }

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun TermsScreenPreview() {
    TodakunTheme {
        TermsScreen(
            termsAgreement = TermsAgreementUiModel(),
            canProceed = false,
            onTermChange = { _ -> },
            onAllTermsChange = {},
            onNextClick = {},
            onBackClick = {},
        )
    }
}
