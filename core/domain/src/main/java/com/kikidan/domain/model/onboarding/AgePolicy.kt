package com.kikidan.domain.model.onboarding

import java.time.LocalDate
import java.time.Period

fun LocalDate.isUnderAge(today: LocalDate = LocalDate.now()): Boolean = Period.between(this, today).years < 14
