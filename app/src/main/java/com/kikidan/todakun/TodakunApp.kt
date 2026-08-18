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
import com.kikidan.navigation.TodakunNavigator
import com.kikidan.navigation.TodakunRoute
import com.kikidan.notification.NotificationRoute
import com.kikidan.notification.component.PushNotificationBanner
import com.kikidan.onboarding.OnboardingRoute
import com.kikidan.onboarding.TermsRoute

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
                        )
                    }

                    entry<TodakunRoute.Chat> { route ->
                        ChatRoute(
                            conversationId = route.conversationId,
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
                            onNewChatClick = { navigator.push(TodakunRoute.Chat()) },
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    entry<TodakunRoute.LuckAction> {
                        LuckActionRoute(snackbarHostState = snackbarHostState)
                    }

                    entry<TodakunRoute.MyPage> {
                        // TODO(#후속이슈): feature:mypage 구현 후 실제 화면 연결
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
