package com.kikidan.navigation

/**
 * `todakun://` 커스텀 스킴 딥링크 문자열을 [TodakunRoute]로 변환한다.
 * 매칭되는 패턴이 없으면 null을 반환하며, 폴백 정책은 호출부(Root)가 결정한다.
 */
fun parseDeepLink(uri: String): TodakunRoute? {
    val path = uri.removePrefix(SCHEME_PREFIX)
    if (path == uri) return null

    return when {
        path == PATH_LUCKY_ACTION -> {
            TodakunRoute.LuckAction
        }

        path.startsWith(PATH_FORTUNE_PREFIX) -> {
            path
                .removePrefix(PATH_FORTUNE_PREFIX)
                .takeIf { it.isNotBlank() }
                ?.let { fortuneId -> TodakunRoute.FortuneReport(fortuneId = fortuneId) }
        }

        path.startsWith(PATH_CHAT_CONVERSATION_PREFIX) -> {
            path
                .removePrefix(PATH_CHAT_CONVERSATION_PREFIX)
                .takeIf { it.isNotBlank() }
                ?.let { conversationId -> TodakunRoute.Chat(conversationId = conversationId) }
        }

        else -> {
            null
        }
    }
}

private const val SCHEME_PREFIX = "todakun://"
private const val PATH_LUCKY_ACTION = "lucky-action"
private const val PATH_FORTUNE_PREFIX = "fortune/"
private const val PATH_CHAT_CONVERSATION_PREFIX = "chat/conversations/"
