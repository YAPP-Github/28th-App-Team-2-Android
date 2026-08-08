package com.kikidan.onboarding.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunTextField
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.onboarding.UserName
import com.kikidan.onboarding.R
import com.kikidan.onboarding.component.OnboardingLayout
import com.kikidan.onboarding.model.OnboardingStep

@Composable
internal fun NameScreen(
    username: UserName,
    canProceed: Boolean,
    onNameChange: (String) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        buildAnnotatedString {
            append(stringResource(R.string.onboarding_name_title1))
            withStyle(SpanStyle(color = TodakunColor.primary700)) {
                append(stringResource(R.string.onboarding_name_title_primary))
            }
            append(stringResource(R.string.onboarding_name_title2))
        }

    val input =
        when (username) {
            is UserName.Valid -> username.name
            is UserName.Invalid.Empty -> ""
            is UserName.Invalid.TooLong -> username.input
            is UserName.Invalid.ContainsSpecialCharacter -> username.input
        }

    val isError = username is UserName.Invalid && username !is UserName.Invalid.Empty
    val errorMessage = if (isError) username.errorMessage() else ""

    OnboardingLayout(
        progress = OnboardingStep.NAME.progress,
        title = title,
        ctaText = stringResource(id = R.string.onboarding_next),
        ctaEnabled = canProceed,
        onCtaClick = onNextClick,
        onBackClick = onBackClick,
        modifier = modifier.imePadding(),
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_name_label),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.black,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TodakunTextField(
            value = input,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(id = R.string.onboarding_name_placeholder),
            isError = isError,
            errorMessage = errorMessage,
        )
    }
}

@Composable
private fun UserName.Invalid.errorMessage(): String =
    when (this) {
        is UserName.Invalid.Empty -> {
            stringResource(id = R.string.onboarding_name_error_empty)
        }

        is UserName.Invalid.TooLong -> {
            stringResource(id = R.string.onboarding_name_error_too_long, UserName.MAX_LENGTH)
        }

        is UserName.Invalid.ContainsSpecialCharacter -> {
            stringResource(id = R.string.onboarding_name_error_special_character)
        }
    }

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun NameScreenPreview() {
    TodakunTheme {
        NameScreen(
            username = UserName.Invalid.ContainsSpecialCharacter("토닥이##"),
            canProceed = false,
            onNameChange = {},
            onNextClick = {},
            onBackClick = {},
        )
    }
}
