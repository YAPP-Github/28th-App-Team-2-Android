package com.kikidan.domain.util

// 갑자년(甲子年) 기준 AD 4년부터 60년 주기로 순환한다.
private val GAN =
    listOf(
        "갑" to "甲",
        "을" to "乙",
        "병" to "丙",
        "정" to "丁",
        "무" to "戊",
        "기" to "己",
        "경" to "庚",
        "신" to "辛",
        "임" to "壬",
        "계" to "癸",
    )
private val JI =
    listOf(
        "자" to "子",
        "축" to "丑",
        "인" to "寅",
        "묘" to "卯",
        "진" to "辰",
        "사" to "巳",
        "오" to "午",
        "미" to "未",
        "신" to "申",
        "유" to "酉",
        "술" to "戌",
        "해" to "亥",
    )

fun yearToGanji(year: Int): String {
    val index = ((year - 4) % 60 + 60) % 60
    val (ganKr, ganHanja) = GAN[index % 10]
    val (jiKr, jiHanja) = JI[index % 12]
    return "$ganKr${jiKr}년($ganHanja${jiHanja}年)"
}
