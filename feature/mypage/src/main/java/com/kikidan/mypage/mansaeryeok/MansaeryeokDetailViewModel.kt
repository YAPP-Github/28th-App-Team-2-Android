package com.kikidan.mypage.mansaeryeok

import androidx.lifecycle.ViewModel
import com.kikidan.designsystem.R
import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.User
import com.kikidan.mypage.mansaeryeok.model.FiveElementDistribution
import com.kikidan.mypage.mansaeryeok.model.FiveElementStatus
import com.kikidan.mypage.mansaeryeok.model.MansaeryeokDetailSideEffect
import com.kikidan.mypage.mansaeryeok.model.MansaeryeokDetailUiModel
import com.kikidan.mypage.mansaeryeok.model.MansaeryeokDetailUiState
import com.kikidan.mypage.mansaeryeok.model.SajuPillarDetail
import com.kikidan.mypage.mansaeryeok.model.TenGod
import com.kikidan.mypage.mansaeryeok.model.TwelveSinsal
import com.kikidan.mypage.mansaeryeok.model.TwelveUnseong
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

/**
 * 십성/지장간/12운성/12신살/오행 분포를 계산해 주는 UseCase·API가 아직 없어
 * Figma 목업 텍스트를 그대로 사용한다. 실제 계산/연동은 Swagger 확인 후 별도 작업 필요.
 */
@HiltViewModel
class MansaeryeokDetailViewModel
    @Inject
    constructor() :
    ViewModel(),
        ContainerHost<MansaeryeokDetailUiState, MansaeryeokDetailSideEffect> {
        override val container: Container<MansaeryeokDetailUiState, MansaeryeokDetailSideEffect> =
            container(MansaeryeokDetailUiState.Success(MockMansaeryeokDetail))
    }

private val MockMansaeryeokDetail =
    MansaeryeokDetailUiModel(
        user =
            User(
                id = "mock",
                name = "토닥이",
                gender = Gender.FEMALE,
                birth =
                    Birth(
                        dateType = DateType.SOLAR,
                        date = LocalDate.of(2001, 5, 30),
                        time = BirthTime.O,
                    ),
            ),
        pillars =
            listOf(
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.SIN, JiJi.MI),
                    pillarLabelRes = R.string.mansaeryeok_pillar_hour,
                    periodLabelRes = R.string.mansaeryeok_period_late,
                    topTenGod = TenGod.SIKSIN,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "을계무",
                    twelveUnseong = TwelveUnseong.YANG,
                    twelveSinsal = TwelveSinsal.HWAGAESAL,
                ),
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.GI, JiJi.SA),
                    pillarLabelRes = R.string.mansaeryeok_pillar_day,
                    periodLabelRes = R.string.mansaeryeok_period_prime,
                    topTenGod = TenGod.ILWON,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "무경병",
                    twelveUnseong = TwelveUnseong.TAE,
                    twelveSinsal = TwelveSinsal.GEOPSAL,
                ),
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.GYE, JiJi.MYO),
                    pillarLabelRes = R.string.mansaeryeok_pillar_month,
                    periodLabelRes = R.string.mansaeryeok_period_youth,
                    topTenGod = TenGod.PYEONJAE,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "계신기",
                    twelveUnseong = TwelveUnseong.GWANDAE,
                    twelveSinsal = TwelveSinsal.BANANSAL,
                ),
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.JEONG, JiJi.CHUK),
                    pillarLabelRes = R.string.mansaeryeok_pillar_year,
                    periodLabelRes = R.string.mansaeryeok_period_early,
                    topTenGod = TenGod.PYEONIN,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "을계무",
                    twelveUnseong = TwelveUnseong.YANG,
                    twelveSinsal = TwelveSinsal.HWAGAESAL,
                ),
            ),
        fiveElements =
            listOf(
                FiveElementDistribution(Ohaeng.MOK, count = 1, status = FiveElementStatus.LACKING),
                FiveElementDistribution(Ohaeng.HWA, count = 2, status = FiveElementStatus.MODERATE),
                FiveElementDistribution(Ohaeng.TO, count = 3, status = FiveElementStatus.ABUNDANT),
                FiveElementDistribution(Ohaeng.GEUM, count = 1, status = FiveElementStatus.MODERATE),
                FiveElementDistribution(Ohaeng.SU, count = 1, status = FiveElementStatus.LACKING),
            ),
    )
