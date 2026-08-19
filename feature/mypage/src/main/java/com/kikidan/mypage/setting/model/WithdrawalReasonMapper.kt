package com.kikidan.mypage.setting.model

import com.kikidan.domain.model.user.WithdrawalReason

private val WithdrawalReasonOrder =
    listOf(
        WithdrawalReason.CONTENT_INAPPROPRIATE,
        WithdrawalReason.CHATBOT_UNSATISFACTORY,
        WithdrawalReason.LOW_USAGE,
    )

fun List<String>.toWithdrawalReason(selectedReason: String): WithdrawalReason? =
    WithdrawalReasonOrder.getOrNull(indexOf(selectedReason))
