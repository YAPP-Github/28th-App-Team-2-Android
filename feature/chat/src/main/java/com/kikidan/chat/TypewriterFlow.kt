package com.kikidan.chat

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 실기기에서 조정할 튜닝 노브. 체감 속도는 서버가 보내는 청크 크기에 좌우된다.
internal const val TYPING_TICK_MS = 16L          // 60fps 프레임 간격
private const val CATCH_UP_DIVISOR = 12          // 남은 글자의 1/12를 매 틱 추가 방출

/**
 * 도착 속도(네트워크)와 표시 속도(화면)를 분리한다.
 *
 * 업스트림 청크를 버퍼에 쌓고 tick마다 조금씩 잘라 "지금까지 보여줄 전체 텍스트"를 방출한다.
 * 백로그가 작으면 한 글자씩, 쌓이면 자동으로 빨라져 생성 종료 후 혼자 타이핑하는 지연을 막는다.
 * 업스트림이 끝나도 버퍼가 다 빌 때까지 방출을 이어간 뒤 완료한다.
 *
 * channelFlow를 사용하는 이유: 컨슈머가 취소되면 프로듀서 스코프와 그 자식(upstream launch)이
 * 함께 취소돼 업스트림 자원이 정상 해제된다.
 */
internal fun Flow<String>.typewriter(tickMillis: Long = TYPING_TICK_MS): Flow<String> =
    channelFlow {
        val buffered = MutableStateFlow("")
        val upstream = launch { this@typewriter.collect { chunk -> buffered.update { it + chunk } } }
        var shown = 0
        while (true) {
            // isActive를 value보다 먼저 읽어야 한다. 순서를 바꾸면
            // "value를 읽은 직후 도착한 마지막 청크"를 못 보고 종료할 수 있다.
            val upstreamDone = !upstream.isActive
            val full = buffered.value
            when {
                shown < full.length -> {
                    shown = (shown + 1 + (full.length - shown) / CATCH_UP_DIVISOR)
                        .coerceAtMost(full.length)
                    // ponytail: 틱마다 substring이라 전체 O(n^2). 수천 자 답변까지는 무시 가능.
                    send(full.substring(0, shown))
                }
                upstreamDone -> return@channelFlow
                else -> Unit
            }
            delay(tickMillis)
        }
    }
