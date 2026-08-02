package com.kikidan.domain.model.onboarding

enum class OnboardingTerm(
    val required: Boolean,
) {
    SERVICE(required = true),
    PRIVACY(required = true),
    AI_DATA_TRANSFER(required = true),
    MARKETING(required = false),
}
