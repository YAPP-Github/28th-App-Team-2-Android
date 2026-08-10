package com.kikidan.domain.model.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class UserNameTest {
    @Test
    fun `한글 이름은 통과한다`() {
        assertEquals(UserName.Valid("토닥이"), UserName.from("토닥이"))
    }

    @Test
    fun `영문과 숫자, 공백이 섞여도 통과한다`() {
        assertEquals(UserName.Valid("Todak un 2"), UserName.from("Todak un 2"))
    }

    @Test
    fun `빈 문자열은 Empty다`() {
        assertEquals(UserName.Invalid.Empty, UserName.from(""))
    }

    @Test
    fun `공백만 있으면 Empty다`() {
        assertEquals(UserName.Invalid.Empty, UserName.from("   "))
    }

    @Test
    fun `특수문자가 포함되면 ContainsSpecialCharacter다`() {
        assertEquals(UserName.Invalid.ContainsSpecialCharacter("토닥이##"), UserName.from("토닥이##"))
    }

    @Test
    fun `이모지도 특수문자로 걸러진다`() {
        assertEquals(UserName.Invalid.ContainsSpecialCharacter("토닥이🙂"), UserName.from("토닥이🙂"))
    }

    @Test
    fun `최대 길이까지는 통과한다`() {
        val name = "가".repeat(UserName.MAX_LENGTH)

        assertEquals(UserName.Valid(name), UserName.from(name))
    }

    @Test
    fun `최대 길이를 넘으면 TooLong이다`() {
        val name = "가".repeat(UserName.MAX_LENGTH + 1)

        assertEquals(UserName.Invalid.TooLong(name), UserName.from(name))
    }

    @Test
    fun `앞뒤 공백은 길이 계산에서 제외된다`() {
        val trimmed = "가".repeat(UserName.MAX_LENGTH)
        val name = " $trimmed "

        assertEquals(UserName.Valid(trimmed), UserName.from(name))
    }
}
