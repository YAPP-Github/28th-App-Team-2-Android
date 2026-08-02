package com.kikidan.onboarding

/**
 * 가입 플로우의 단계. 선언 순서가 곧 진행 순서다.
 *
 * @param progress 프로그레스바 채움 비율(Figma 실측, 트랙 316dp 기준). 첫 스텝(TERMS)은 진행률 대신
 * 라벨 텍스트를 보여주므로 null이다.
 */
enum class OnboardingStep(
    val progress: Float?,
) {
    TERMS(null),
    NAME(1f / 3f),
    BIRTH_INFO(2f / 3f),
    EXTRA_QUESTION(1f),
    ;

    val previous: OnboardingStep?
        get() = entries.getOrNull(ordinal - 1)

    val next: OnboardingStep?
        get() = entries.getOrNull(ordinal + 1)
}
