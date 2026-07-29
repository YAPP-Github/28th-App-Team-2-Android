package com.kikidan.data_local.datasource

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kikidan.domain.model.auth.AuthToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class LocalTokenDataSourceTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var sut: LocalTokenDataSourceImpl

    @Before
    fun setUp() {
        val file = tmpFolder.newFile("test_auth_token.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = testScope) { file }
        sut = LocalTokenDataSourceImpl(dataStore)
    }

    @After
    fun tearDown() {
        testScope.testScheduler.advanceUntilIdle()
    }

    @Test
    fun `saveToken으로_저장한_토큰이_getToken으로_그대로_조회된다`() =
        testScope.runTest {
            // given
            val token = AuthToken("access-1", "refresh-1")

            // when
            sut.saveToken(token)

            // then
            assertEquals(token, sut.getToken())
        }

    @Test
    fun `clearToken을_호출하면_getToken과_observeToken이_모두_null이_된다`() =
        testScope.runTest {
            // given
            sut.saveToken(AuthToken("access-1", "refresh-1"))

            // when
            sut.clearToken()

            // then
            assertNull(sut.getToken())
            assertNull(sut.observeToken().first())
        }

    // access_token 키만 직접 쓰는 부분 손상 상태는 DataStore 공개 API로 재현 불가(atomic edit이라
    // partial write가 없음). 대신 저장 후 클리어해 "두 키 모두 없음"을 만들어 같은 불변식(두 키가
    // 모두 있어야 유효)을 검증한다.
    @Test
    fun `두_토큰_키가_모두_없으면_observeToken이_null을_방출해_부분_토큰을_유효로_오인하지_않는다`() =
        testScope.runTest {
            // given
            sut.saveToken(AuthToken("a", "r"))
            sut.clearToken()

            // when & then
            assertNull(sut.observeToken().first())
        }
}
