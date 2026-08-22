package com.kikidan.domain.model.onboarding

enum class OnboardingTerm(
    val required: Boolean,
    val link: String,
) {
    SERVICE(
        required = true,
        link = "https://ash-topaz-463.notion.site/3b081c67484680aca6e5ec1d463c670d?source=copy_link",
    ),
    PRIVACY(
        required = true,
        link = "https://ash-topaz-463.notion.site/3b081c6748468045a408eb20d27e2342?source=copy_link",
    ),
    AI_DATA_TRANSFER(
        required = true,
        link = "https://ash-topaz-463.notion.site/AI-3b281c67484680aaad1bf2c384d016e1?source=copy_link",
    ),
    MARKETING(
        required = false,
        link = "https://ash-topaz-463.notion.site/3b281c67484680e1812acb94654fdc31",
    ),
}
