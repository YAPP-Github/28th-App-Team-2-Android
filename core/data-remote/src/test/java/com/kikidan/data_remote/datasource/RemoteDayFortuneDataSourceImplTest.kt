package com.kikidan.data_remote.datasource

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.fortune.FortuneCategory
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RemoteDayFortuneDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private val purpose = DayFortunePurpose.TRAVEL
    private val targetDates = listOf(LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12))

    private fun buildSut(handler: MockRequestHandler): RemoteDayFortuneDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteDayFortuneDataSourceImpl(Lazy { client })
    }

    @Test
    fun `요청_경로와_메서드가_api_v1_day-fortunes_POST_이다`() =
        runTest {
            // given
            var capturedUrl: String? = null
            var capturedMethod: HttpMethod? = null
            val sut =
                buildSut { request ->
                    capturedUrl = request.url.encodedPath
                    capturedMethod = request.method
                    respond(
                        content =
                            """{"success":true,"code":"200","message":"ok","data":[]}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            // when
            sut.postDayFortunes(purpose, targetDates)

            // then
            assertEquals("/api/v1/day-fortunes", capturedUrl)
            assertEquals(HttpMethod.Post, capturedMethod)
        }

    @Test
    fun `요청_body의_purpose는_enum_name_문자열이고_targetDates는_yyyy-MM-dd_배열이다`() =
        runTest {
            // given
            var capturedBody: String? = null
            val sut =
                buildSut { request ->
                    capturedBody = String((request.body as OutgoingContent.ByteArrayContent).bytes())
                    respond(
                        content = """{"success":true,"code":"200","message":"ok","data":[]}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            // when
            sut.postDayFortunes(purpose, targetDates)

            // then
            assertEquals(
                """{"purpose":"TRAVEL","targetDates":["2026-08-10","2026-08-12"]}""",
                capturedBody,
            )
        }

    @Test
    fun `정상_응답이면_targetDate가_LocalDate로_fortuneCategory가_enum으로_매핑된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok","data":[""" +
                    """{"id":"id-1","purpose":"TRAVEL","targetDate":"2026-08-10","score":85,""" +
                    """"title":"title","content":"content",""" +
                    """"fortuneCategories":[{"fortuneCategory":"MONEY","star":3}]}]}"""
            val sut =
                buildSut {
                    respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
                }

            // when
            val result = sut.postDayFortunes(purpose, targetDates)

            // then
            assertEquals(1, result.size)
            val dayFortune = result.first()
            assertEquals(LocalDate.of(2026, 8, 10), dayFortune.targetDate)
            assertEquals(DayFortunePurpose.TRAVEL, dayFortune.purpose)
            assertEquals(FortuneCategory.MONEY, dayFortune.categoryStars.first().category)
            assertEquals(3, dayFortune.categoryStars.first().star)
        }

    @Test
    fun `data가_null이면_IllegalArgumentException이_throw된다`() =
        runTest {
            // given
            val body = """{"success":true,"code":"200","message":"ok"}"""
            val sut =
                buildSut {
                    respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
                }

            // when
            val result = runCatching { sut.postDayFortunes(purpose, targetDates) }

            // then
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }

    @Test
    fun `알_수_없는_fortuneCategory는_해당_항목만_제외되고_나머지는_정상_매핑된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok","data":[""" +
                    """{"id":"id-1","purpose":"TRAVEL","targetDate":"2026-08-10","score":85,""" +
                    """"title":"title","content":"content",""" +
                    """"fortuneCategories":[{"fortuneCategory":"UNKNOWN_CATEGORY","star":1},""" +
                    """{"fortuneCategory":"MONEY","star":3}]}]}"""
            val sut =
                buildSut {
                    respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
                }

            // when
            val result = sut.postDayFortunes(purpose, targetDates)

            // then
            assertEquals(1, result.first().categoryStars.size)
            assertEquals(
                FortuneCategory.MONEY,
                result
                    .first()
                    .categoryStars
                    .first()
                    .category,
            )
        }

    @Test
    fun `알_수_없는_purpose이면_예외가_throw된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok","data":[""" +
                    """{"id":"id-1","purpose":"UNKNOWN_PURPOSE","targetDate":"2026-08-10","score":85,""" +
                    """"title":"title","content":"content","fortuneCategories":[]}]}"""
            val sut =
                buildSut {
                    respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
                }

            // when
            val result = runCatching { sut.postDayFortunes(purpose, targetDates) }

            // then
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }

    @Test
    fun `5xx_응답이면_예외가_삼켜지지_않고_그대로_throw된다`() =
        runTest {
            // given
            val sut =
                buildSut {
                    respond("", HttpStatusCode.InternalServerError)
                }

            // when
            val result = runCatching { sut.postDayFortunes(purpose, targetDates) }

            // then
            assertTrue(result.isFailure)
        }

    @Test
    fun `getDayFortune_요청_경로와_메서드가_api_v1_day-fortunes_id_GET_이다`() =
        runTest {
            // given
            var capturedUrl: String? = null
            var capturedMethod: HttpMethod? = null
            val sut =
                buildSut { request ->
                    capturedUrl = request.url.encodedPath
                    capturedMethod = request.method
                    respond(
                        content =
                            """{"success":true,"code":"200","message":"ok","data":""" +
                                """{"id":"id-1","purpose":"TRAVEL","targetDate":"2026-08-10","score":85,""" +
                                """"title":"title","content":"content","fortuneCategories":[]}}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            // when
            sut.getDayFortune("id-1")

            // then
            assertEquals("/api/v1/day-fortunes/id-1", capturedUrl)
            assertEquals(HttpMethod.Get, capturedMethod)
        }

    @Test
    fun `getDayFortune_정상_응답이면_targetDate가_LocalDate로_매핑된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok","data":""" +
                    """{"id":"id-1","purpose":"TRAVEL","targetDate":"2026-08-10","score":85,""" +
                    """"title":"title","content":"content",""" +
                    """"fortuneCategories":[{"fortuneCategory":"MONEY","star":3}]}}"""
            val sut =
                buildSut {
                    respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
                }

            // when
            val result = sut.getDayFortune("id-1")

            // then
            assertEquals(LocalDate.of(2026, 8, 10), result.targetDate)
            assertEquals(DayFortunePurpose.TRAVEL, result.purpose)
            assertEquals(FortuneCategory.MONEY, result.categoryStars.first().category)
        }

    @Test
    fun `getDayFortune_data가_null이면_IllegalArgumentException이_throw된다`() =
        runTest {
            // given
            val body = """{"success":true,"code":"200","message":"ok"}"""
            val sut =
                buildSut {
                    respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
                }

            // when
            val result = runCatching { sut.getDayFortune("id-1") }

            // then
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }
}
