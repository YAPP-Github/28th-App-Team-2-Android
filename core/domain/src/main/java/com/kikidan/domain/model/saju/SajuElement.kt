package com.kikidan.domain.model.saju

enum class Ohaeng(
    val displayName: String,
    val hanja: String,
) {
    MOK("목", "木"),
    HWA("화", "火"),
    TO("토", "土"),
    GEUM("금", "金"),
    SU("수", "水"),
}

enum class Eumyang(
    val displayName: String,
    val hanja: String,
) {
    YANG("양", "陽"),
    EUM("음", "陰"),
}

enum class CheonGan(
    val displayName: String,
    val hanja: String,
    val ohaeng: Ohaeng,
    val eumyang: Eumyang,
) {
    GAP("갑", "甲", Ohaeng.MOK, Eumyang.YANG),
    EUL("을", "乙", Ohaeng.MOK, Eumyang.EUM),
    BYEONG("병", "丙", Ohaeng.HWA, Eumyang.YANG),
    JEONG("정", "丁", Ohaeng.HWA, Eumyang.EUM),
    MU("무", "戊", Ohaeng.TO, Eumyang.YANG),
    GI("기", "己", Ohaeng.TO, Eumyang.EUM),
    GYEONG("경", "庚", Ohaeng.GEUM, Eumyang.YANG),
    SIN("신", "辛", Ohaeng.GEUM, Eumyang.EUM),
    IM("임", "壬", Ohaeng.SU, Eumyang.YANG),
    GYE("계", "癸", Ohaeng.SU, Eumyang.EUM),
}

enum class JiJi(
    val displayName: String,
    val hanja: String,
    val ohaeng: Ohaeng,
    val eumyang: Eumyang,
) {
    JA("자", "子", Ohaeng.SU, Eumyang.YANG),
    CHUK("축", "丑", Ohaeng.TO, Eumyang.EUM),
    IN("인", "寅", Ohaeng.MOK, Eumyang.YANG),
    MYO("묘", "卯", Ohaeng.MOK, Eumyang.EUM),
    JIN("진", "辰", Ohaeng.TO, Eumyang.YANG),
    SA("사", "巳", Ohaeng.HWA, Eumyang.EUM),
    O("오", "午", Ohaeng.HWA, Eumyang.YANG),
    MI("미", "未", Ohaeng.TO, Eumyang.EUM),
    SIN("신", "申", Ohaeng.GEUM, Eumyang.YANG),
    YU("유", "酉", Ohaeng.GEUM, Eumyang.EUM),
    SUL("술", "戌", Ohaeng.TO, Eumyang.YANG),
    HAE("해", "亥", Ohaeng.SU, Eumyang.EUM),
}