package com.kikidan.domain.model.user

import java.time.LocalDate
import java.time.LocalTime

data class User(
    val id: String,
    val name: String,
    val job: Job,
    val relationshipStatus: RelationshipStatus,
    val favoriteFortuneCategories: List<String>,
    val gender: Gender,
    val birth: Birth,
) {
    init {
        require(favoriteFortuneCategories.size <= 5) {
            "관심 운세 카테고리 목록은 5개 이하여야 합니다"
        }
    }
}

enum class Gender {
    MALE,
    FEMALE,
}

data class Birth(
    val dateType: DateType,
    val date: LocalDate,
    val time: BirthTime,
)

enum class DateType {
    LUNAR,
    SOLAR,
}

enum class BirthTime(
    val displayName: String,
    val hanjaName: String?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
) {
    JA("자시", "子時", LocalTime.of(23, 0), LocalTime.of(1, 0)),
    CHUK("축시", "丑時", LocalTime.of(1, 0), LocalTime.of(3, 0)),
    IN("인시", "寅時", LocalTime.of(3, 0), LocalTime.of(5, 0)),
    MYO("묘시", "卯時", LocalTime.of(5, 0), LocalTime.of(7, 0)),
    JIN("진시", "辰時", LocalTime.of(7, 0), LocalTime.of(9, 0)),
    SA("사시", "巳時", LocalTime.of(9, 0), LocalTime.of(11, 0)),
    O("오시", "午時", LocalTime.of(11, 0), LocalTime.of(13, 0)),
    MI("미시", "未時", LocalTime.of(13, 0), LocalTime.of(15, 0)),
    SIN("신시", "申時", LocalTime.of(15, 0), LocalTime.of(17, 0)),
    YU("유시", "酉時", LocalTime.of(17, 0), LocalTime.of(19, 0)),
    SUL("술시", "戌時", LocalTime.of(19, 0), LocalTime.of(21, 0)),
    HAE("해시", "亥時", LocalTime.of(21, 0), LocalTime.of(23, 0)),
    UNKNOWN("모름", null, null, null),
}

enum class Job {
    STUDENT,
    WORKER,
    FREELANCER,
    JOBSEEKER,
    HOMEMAKER,
    LEAVER
}

enum class RelationshipStatus {
    SOLO,
    DATING,
    MARRY,
    REMARRY
}
