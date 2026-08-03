package com.kikidan.onboarding.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunProgressBar
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
internal fun OnboardingScaffold(
    progress: Float?,
    title: AnnotatedString,
    ctaText: String,
    ctaEnabled: Boolean,
    onCtaClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    progressLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = TodakunColor.white,
        topBar = {
            if (progress != null) {
                TodakunProgressBar(
                    progress = progress,
                    onBackClick = onBackClick,
                    modifier = Modifier.statusBarsPadding(),
                )
            } else if (progressLabel != null) {
                TermsHeader(
                    label = progressLabel,
                    onBackClick = onBackClick,
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        },
        bottomBar = {
            Column(
                modifier =
                    Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                PrimaryButton(
                    text = ctaText,
                    onClick = onCtaClick,
                    size = TodakunButtonSize.Large,
                    enabled = ctaEnabled,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = TodakunTypography.heading2SemiBold,
                color = TodakunColor.black,
            )
            Spacer(modifier = Modifier.height(40.dp))
            content()
        }
    }
}

@Composable
private fun TermsHeader(
    label: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = stringResource(id = R.string.header_back_content_description),
                tint = TodakunColor.gray400,
                modifier = Modifier.size(width = 8.dp, height = 16.dp),
            )
        }
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = label,
            style = TodakunTypography.body2Medium,
            color = TodakunColor.gray500,
        )
    }
}
