package com.kikidan.onboarding.model

/**
 * 가입 플로우(약관 동의 이후)의 단계. 선언 순서가 곧 진행 순서다.
 *
 * @param progress 프로그레스바 채움 비율(Figma 실측, 트랙 316dp 기준).
 */
enum class OnboardingStep(
    val progress: Float?,
) {
    NAME(1f / 3f),
    BIRTH_INFO(2f / 3f),
    EXTRA_QUESTION(1f),
    COMPLETE(null),
    ;

    val previous: OnboardingStep?
        get() =
            when (this) {
                COMPLETE -> null
                else -> entries.getOrNull(ordinal - 1)
            }

    val next: OnboardingStep?
        get() = entries.getOrNull(ordinal + 1)
}
