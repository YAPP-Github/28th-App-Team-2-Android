package com.kikidan.chat

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * 도착 속도(네트워크)와 표시 속도(화면)를 분리한다.
 * 업스트림 청크를 버퍼에 쌓고 tick마다 조금씩 잘라 지금까지 보여줄 전체 텍스"를 방출한다.
 */
internal fun Flow<String>.typewriter(tickMillis: Long = 16L): Flow<String> =
    channelFlow {
        val buffered = StringBuffer()
        val upstream = launch { this@typewriter.collect { chunk -> buffered.append(chunk) } }
        var shown = 0
        while (true) {
            val upstreamDone = !upstream.isActive
            when {
                shown < buffered.length -> {
                    shown =
                        (((buffered.length - shown) / CATCH_UP_DIVISOR) + shown + 1)
                            .coerceAtMost(buffered.length)
                    send(buffered.substring(0, shown))
                }

                upstreamDone -> {
                    return@channelFlow
                }
            }
            delay(tickMillis)
        }
    }

// 남은 글자의 1/32 를 매 틱 추가 방출,
// 약 0.35초에 남은 buffer의 절반을 채우는 속도, tickMillis 파라미터와 더불어 실기기에서 체크 후 조정 요망
private const val CATCH_UP_DIVISOR = 32
