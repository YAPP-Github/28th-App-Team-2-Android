package com.kikidan.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class YearGanjiFormatterTest {
    @Test
    fun `1984년은_갑자년이다`() {
        assertEquals("갑자년(甲子年)", yearToGanji(1984))
    }

    @Test
    fun `2024년은_갑진년이다`() {
        assertEquals("갑진년(甲辰年)", yearToGanji(2024))
    }

    @Test
    fun `2026년은_병오년이다`() {
        assertEquals("병오년(丙午年)", yearToGanji(2026))
    }
}
