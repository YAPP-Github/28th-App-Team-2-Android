package com.kikidan.data.repository

import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.User
import com.kikidan.domain.repository.UserRepository
import java.time.LocalDate
import javax.inject.Inject

class FakeUserRepository
    @Inject
    constructor() : UserRepository {
        override suspend fun getUserInfo(): Result<User> =
            Result.success(
                User(
                    id = "fake-user-id",
                    gender = Gender.FEMALE,
                    birth =
                        Birth(
                            dateType = DateType.SOLAR,
                            date = LocalDate.of(1999, 2, 13),
                            time = BirthTime.SA,
                        ),
                ),
            )
    }
