package com.kikidan.domain.model.onboarding

/**
 * 이름 입력 검증 결과. 실패 사유별로 화면이 다른 안내 문구를 고를 수 있도록 타입으로 구분한다.
 */
sealed interface UserName {
    data class Valid(
        val name: String,
    ) : UserName

    sealed interface Invalid : UserName {
        data object Empty : Invalid

        data class TooLong(
            val input: String,
        ) : Invalid

        data class ContainsSpecialCharacter(
            val input: String,
        ) : Invalid
    }

    companion object {
        const val MAX_LENGTH = 10
        private val ALLOWED_PATTERN = Regex("^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9 ]+$")

        fun from(raw: String): UserName {
            val trimmed = raw.trim()
            return when {
                trimmed.isEmpty() -> Invalid.Empty
                trimmed.length > MAX_LENGTH -> Invalid.TooLong(raw)
                !ALLOWED_PATTERN.matches(trimmed) -> Invalid.ContainsSpecialCharacter(raw)
                else -> Valid(trimmed)
            }
        }
    }
}
