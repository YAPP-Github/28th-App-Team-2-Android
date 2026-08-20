package com.kikidan.todakun

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kikidan.auth.LoginRoute
import com.kikidan.chat.ChatRoute
import com.kikidan.chat.HistoryRoute
import com.kikidan.designsystem.component.bottomnavigation.TodakunBottomNavigation
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.notification.PushNotificationEvent
import com.kikidan.domain.notification.PushNotificationEventFlow
import com.kikidan.home.FortuneReportRoute
import com.kikidan.home.HomeRoute
import com.kikidan.luckaction.LuckActionRoute
import com.kikidan.mypage.edit.ui.MyPageEditRoute
import com.kikidan.mypage.home.ui.MyPageHomeRoute
import com.kikidan.mypage.mansaeryeok.ui.MansaeryeokDetailRoute
import com.kikidan.mypage.notification.ui.NotificationSettingRoute
import com.kikidan.mypage.partner.form.ui.PartnerSajuFormRoute
import com.kikidan.mypage.partner.ui.PartnerSajuManagementRoute
import com.kikidan.mypage.setting.ui.AppSettingRoute
import com.kikidan.mypage.setting.ui.AppSettingWithdrawalNoticeRoute
import com.kikidan.mypage.setting.ui.AppSettingWithdrawalRoute
import com.kikidan.navigation.TodakunNavigator
import com.kikidan.navigation.TodakunRoute
import com.kikidan.notification.NotificationRoute
import com.kikidan.notification.component.PushNotificationBanner
import com.kikidan.onboarding.OnboardingRoute
import com.kikidan.onboarding.TermsRoute
import com.kikidan.sajucontents.CompatibilityEntryRoute
import com.kikidan.sajucontents.CompatibilityResultRoute
import com.kikidan.sajucontents.DateFortuneInputRoute
import com.kikidan.sajucontents.DateFortuneResultRoute
import com.kikidan.sajucontents.YearFortuneResultRoute
import com.kikidan.sajucontents.YearSelectionRoute

@Composable
fun TodakunApp(
    deepLinkRoute: TodakunRoute?,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(TodakunRoute.Login)
    val navigator = remember(backStack) { TodakunNavigator(backStack) }
    val snackbarHostState = remember { SnackbarHostState() }
    var inAppEvent by remember { mutableStateOf<PushNotificationEvent?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            PushNotificationEventFlow.events.collect { event -> inAppEvent = event }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = TodakunColor.white,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            navigator.currentTab?.let { item ->
                TodakunBottomNavigation(
                    modifier = Modifier.navigationBarsPadding(),
                    selectedItem = item,
                    onItemSelect = navigator::selectTab,
                )
            }
        },
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            popTransitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            },
            predictivePopTransitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            },
            entryProvider =
                entryProvider {
                    entry<TodakunRoute.Login> {
                        LoginRoute(
                            onAuthSuccess = { result ->
                                if (result.newMember) {
                                    navigator.resetTo(
                                        TodakunRoute.Terms(
                                            result.onboardingToken?.value.orEmpty(),
                                        ),
                                    )
                                } else {
                                    navigator.resetTo(TodakunRoute.Home)
                                    if (deepLinkRoute != null) {
                                        navigator.push(deepLinkRoute)
                                    }
                                }
                            },
                            onAuthPass = {
                                navigator.resetTo(TodakunRoute.Home)
                                if (deepLinkRoute != null) {
                                    navigator.push(deepLinkRoute)
                                }
                            },
                        )
                    }

                    entry<TodakunRoute.Terms> { route ->
                        TermsRoute(
                            onNavigateOnboarding = {
                                navigator.push(TodakunRoute.Onboarding(route.onboardingToken))
                            },
                            onExit = { navigator.resetTo(TodakunRoute.Login) },
                        )
                    }

                    entry<TodakunRoute.Onboarding> { route ->
                        OnboardingRoute(
                            onFinish = { navigator.resetTo(TodakunRoute.Home) },
                            onNavigateTerm = { navigator.goBack() },
                            onboardingToken = OnboardingToken(route.onboardingToken),
                        )
                    }

                    entry<TodakunRoute.Home> {
                        HomeRoute(
                            snackbarHostState = snackbarHostState,
                            onNavigateToLuckAction = { navigator.resetTo(TodakunRoute.LuckAction) },
                            onNavigateToReport = { fortuneId ->
                                navigator.push(TodakunRoute.FortuneReport(fortuneId))
                            },
                            onNavigateToNotice = { navigator.push(TodakunRoute.Notification) },
                            onNavigateToCompatibility = { navigator.push(TodakunRoute.CompatibilityInput) },
                            onNavigateToDateFortune = { navigator.push(TodakunRoute.DateFortuneInput) },
                            onNavigateToYearFortune = { navigator.push(TodakunRoute.YearFortuneInput) },
                        )
                    }

                    entry<TodakunRoute.Chat> { route ->
                        ChatRoute(
                            conversationId = route.conversationId,
                            skipSplash = route.skipSplash,
                            onCloseClick = { navigator.goBack() },
                            onNavigateToHistory = { navigator.push(TodakunRoute.ChatHistory) },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    entry<TodakunRoute.ChatHistory> {
                        HistoryRoute(
                            onBackClick = { navigator.goBack() },
                            onNavigateToChat = { conversationId ->
                                navigator.push(TodakunRoute.Chat(conversationId))
                            },
                            onNewChatClick = { navigator.push(TodakunRoute.Chat(skipSplash = true)) },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    entry<TodakunRoute.LuckAction> {
                        LuckActionRoute(snackbarHostState = snackbarHostState)
                    }

                    entry<TodakunRoute.MyPage> {
                        MyPageHomeRoute(
                            modifier = Modifier.systemBarsPadding(),
                            onNavigateToEdit = { navigator.push(TodakunRoute.MyPageEdit) },
                            onNavigateToMansaeryeok = { navigator.push(TodakunRoute.Mansaeryeok) },
                            onNavigateToPartnerSajuManagement = {
                                navigator.push(TodakunRoute.PartnerSajuManagement)
                            },
                            onNavigateToNotificationSetting = {
                                navigator.push(TodakunRoute.NotificationSetting)
                            },
                            onNavigateToAppSetting = { navigator.push(TodakunRoute.AppSetting) },
                            onNavigateToLogin = { navigator.resetTo(TodakunRoute.Login) },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    entry<TodakunRoute.MyPageEdit> {
                        MyPageEditRoute(
                            onNavigateBack = { navigator.goBack() },
                        )
                    }

                    entry<TodakunRoute.Mansaeryeok> {
                        MansaeryeokDetailRoute(onNavigateBack = { navigator.goBack() })
                    }

                    entry<TodakunRoute.NotificationSetting> {
                        NotificationSettingRoute(onNavigateBack = { navigator.goBack() })
                    }

                    entry<TodakunRoute.PartnerSajuManagement> {
                        PartnerSajuManagementRoute(
                            onNavigateBack = { navigator.goBack() },
                            onAddPartnerClick = { navigator.push(TodakunRoute.PartnerSajuForm()) },
                            onEditPartnerClick = { linkId ->
                                navigator.push(TodakunRoute.PartnerSajuForm(linkId))
                            },
                        )
                    }

                    entry<TodakunRoute.PartnerSajuForm> { route ->
                        PartnerSajuFormRoute(
                            onNavigateBack = { navigator.goBack() },
                            partnerLinkId = route.partnerLinkId,
                        )
                    }

                    entry<TodakunRoute.AppSetting> {
                        AppSettingRoute(
                            onNavigateBack = { navigator.goBack() },
                            onWithdrawalClick = { navigator.push(TodakunRoute.AppSettingWithdrawal) },
                        )
                    }

                    entry<TodakunRoute.AppSettingWithdrawal> {
                        AppSettingWithdrawalRoute(
                            onNavigateBack = { navigator.goBack() },
                            onNextClick = { reason, detailReason ->
                                navigator.push(TodakunRoute.AppSettingWithdrawalNotice(reason, detailReason))
                            },
                        )
                    }

                    entry<TodakunRoute.AppSettingWithdrawalNotice> { route ->
                        AppSettingWithdrawalNoticeRoute(
                            onNavigateBack = { navigator.goBack() },
                            reason = route.reason,
                            detailReason = route.detailReason,
                            onWithdrawalSuccess = { navigator.resetTo(TodakunRoute.Login) },
                        )
                    }

                    entry<TodakunRoute.CompatibilityInput> {
                        CompatibilityEntryRoute(
                            snackbarHostState = snackbarHostState,
                            onNavigateBack = { navigator.goBack() },
                            onNavigateMyPage = { navigator.push(TodakunRoute.MyPageEdit) },
                            onNavigateToPartnerForm = { navigator.push(TodakunRoute.PartnerSajuForm()) },
                            onNavigateToResult = { compatibilityId, partnerLinkId ->
                                navigator.push(TodakunRoute.CompatibilityResult(compatibilityId, partnerLinkId))
                            },
                        )
                    }

                    entry<TodakunRoute.CompatibilityResult> { route ->
                        CompatibilityResultRoute(
                            compatibilityId = route.compatibilityId,
                            partnerLinkId = route.partnerLinkId,
                            snackbarHostState = snackbarHostState,
                            onNavigateBack = { navigator.goBack() },
                            onAskTodakClick = { navigator.push(TodakunRoute.Chat()) },
                        )
                    }

                    entry<TodakunRoute.DateFortuneInput> {
                        DateFortuneInputRoute(
                            snackbarHostState = snackbarHostState,
                            onNavigateBack = { navigator.goBack() },
                            onNavigateToResult = { ids -> navigator.push(TodakunRoute.DateFortuneResult(ids)) },
                        )
                    }

                    entry<TodakunRoute.DateFortuneResult> { route ->
                        DateFortuneResultRoute(
                            ids = route.ids,
                            snackbarHostState = snackbarHostState,
                            onNavigateBack = { navigator.goBack() },
                            onAskTodakClick = { navigator.push(TodakunRoute.Chat()) },
                        )
                    }

                    entry<TodakunRoute.YearFortuneInput> {
                        YearSelectionRoute(
                            snackbarHostState = snackbarHostState,
                            onNavigateBack = { navigator.goBack() },
                            onNavigateToResult = { id -> navigator.push(TodakunRoute.YearFortuneResult(id)) },
                        )
                    }

                    entry<TodakunRoute.YearFortuneResult> { route ->
                        YearFortuneResultRoute(
                            id = route.id,
                            snackbarHostState = snackbarHostState,
                            onNavigateBack = { navigator.goBack() },
                            onAskTodakClick = { navigator.push(TodakunRoute.Chat()) },
                        )
                    }

                    entry<TodakunRoute.FortuneReport> { route ->
                        FortuneReportRoute(
                            fortuneId = route.fortuneId,
                            snackbarHostState = snackbarHostState,
                            onNavigateToBack = { navigator.goBack() },
                            onNavigateToLuckAction = { navigator.resetTo(TodakunRoute.LuckAction) },
                            onNavigateToChat = { navigator.push(TodakunRoute.Chat()) },
                        )
                    }

                    entry<TodakunRoute.Notification> {
                        NotificationRoute(
                            onBackClick = { navigator.goBack() },
                            onNavigateToDeepLink = navigator::navigateOrFallbackToDeepLink,
                            snackbarHostState = snackbarHostState,
                        )
                    }
                },
        )
        if (inAppEvent != null) {
            PushNotificationBanner(
                modifier = Modifier.systemBarsPadding(),
                event = inAppEvent,
                onDismiss = { inAppEvent = null },
                onClick = { event ->
                    inAppEvent = null
                    event.deepLink?.let(navigator::navigateOrFallbackToDeepLink)
                },
            )
        }
    }
}
