package com.kikidan.data.repository

import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.Job
import com.kikidan.domain.model.user.RelationshipStatus
import com.kikidan.domain.model.user.User
import com.kikidan.domain.model.user.WithdrawalReason
import com.kikidan.domain.repository.UserRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepository
    @Inject
    constructor() : UserRepository {
        private var user =
            User(
                id = "fake-user-id",
                name = "토닥이",
                gender = Gender.FEMALE,
                job = Job.WORKER,
                relationshipStatus = RelationshipStatus.SOLO,
                birth =
                    Birth(
                        dateType = DateType.SOLAR,
                        date = LocalDate.of(1999, 2, 13),
                        time = BirthTime.SA,
                    ),
            )

        override suspend fun getUserInfo(): Result<User> = Result.success(user)

        override suspend fun updateUserInfo(user: User): Result<Unit> {
            this.user = user
            return Result.success(Unit)
        }

        override suspend fun withdraw(
            reason: WithdrawalReason,
            detail: String?,
        ): Result<Unit> = Result.success(Unit)
    }
