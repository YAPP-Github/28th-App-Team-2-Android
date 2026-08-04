package com.kikidan.chat

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypewriterFlowTest {

    @Test
    fun `단일 청크를 받으면 여러 번 나눠 방출하고 마지막 값이 전체 텍스트와 같다`() = runTest {
        val emitted = mutableListOf<String>()

        flowOf("안녕하세요").typewriter(tickMillis = 1L).collect { emitted.add(it) }

        assertTrue("방출 횟수 >= 2", emitted.size >= 2)
        // 각 방출은 직전 방출의 prefix 확장
        for (i in 1 until emitted.size) {
            assertTrue(
                "emitted[$i]='${emitted[i]}'이 emitted[${i - 1}]='${emitted[i - 1]}'으로 시작해야 함",
                emitted[i].startsWith(emitted[i - 1]),
            )
        }
        assertEquals("안녕하세요", emitted.last())
    }

    @Test
    fun `여러 청크를 순서대로 받으면 누적해서 방출하고 마지막 값이 전체 텍스트와 같다`() = runTest {
        val upstream = flow {
            emit("안")
            emit("녕")
            emit("!")
        }
        val emitted = mutableListOf<String>()

        upstream.typewriter(tickMillis = 1L).collect { emitted.add(it) }

        assertEquals("안녕!", emitted.last())
    }

    @Test
    fun `업스트림이 대량 청크 직후 즉시 완료되면 남은 버퍼가 전부 방출된 뒤 완료된다`() = runTest {
        // 가장 중요한 케이스: 답변 뒷부분 유실 회귀 방지
        val bigChunk = "a".repeat(50)
        val emitted = mutableListOf<String>()

        flowOf(bigChunk).typewriter(tickMillis = 1L).collect { emitted.add(it) }

        assertEquals("버퍼가 다 비워진 뒤 완료: last='${emitted.last()}'", bigChunk, emitted.last())
        assertTrue("업스트림 완료 후에도 여러 번 나눠 방출됨", emitted.size > 1)
    }

    @Test
    fun `빈 업스트림은 방출 없이 정상 완료된다`() = runTest {
        val emitted = mutableListOf<String>()

        emptyFlow<String>().typewriter(tickMillis = 1L).collect { emitted.add(it) }

        assertTrue(emitted.isEmpty())
    }

    @Test
    fun `업스트림이 예외를 throw하면 동일한 예외가 collect 지점으로 전파된다`() = runTest {
        val error = RuntimeException("테스트 오류")
        val upstream = flow<String> { throw error }

        val caught = runCatching {
            upstream.typewriter(tickMillis = 1L).collect { }
        }

        assertTrue(caught.isFailure)
        // kotlinx-coroutines exception recovery may copy the instance; check class + message
        val ex = caught.exceptionOrNull()
        assertTrue("RuntimeException이어야 함", ex is RuntimeException)
        assertEquals(error.message, ex?.message)
    }

    @Test
    fun `1000자 단일 청크는 catch_up 덕분에 1000틱 미만으로 완료된다`() = runTest {
        val bigText = "x".repeat(1000)
        val emitted = mutableListOf<String>()

        flowOf(bigText).typewriter(tickMillis = 1L).collect { emitted.add(it) }

        assertEquals(bigText, emitted.last())
        assertTrue("catch-up 없이는 1000틱, 실제=${emitted.size}", emitted.size < 1000)
    }

    @Test
    fun `collect 취소 시 예외 없이 종료되고 업스트림도 취소된다`() = runTest {
        var upstreamCancelled = false
        val upstream = flow<String> {
            try {
                awaitCancellation()
            } finally {
                upstreamCancelled = true
            }
        }

        val job = launch {
            upstream.typewriter(tickMillis = 1L).collect { }
        }
        // runTest 기본 디스패처는 StandardTestDispatcher라 launch가 즉시 실행되지 않는다.
        // runCurrent()로 delay(tickMillis) 중단 지점까지 진행시킨 뒤 취소해야
        // 업스트림 collect(awaitCancellation)가 실제로 시작된 상태에서 취소를 검증할 수 있다.
        // advanceUntilIdle()은 무한 delay 루프 때문에 타임아웃되므로 쓰지 않는다.
        runCurrent()

        job.cancel()
        job.join()

        assertTrue("job이 취소됨", job.isCancelled)
        assertTrue("업스트림도 취소됨", upstreamCancelled)
    }
}
