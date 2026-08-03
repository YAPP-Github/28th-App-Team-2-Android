package com.kikidan.mypage.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.home.MyPageHomeViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun MyPageHomeRoute(
    viewModel: MyPageHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()

    MyPageHomeScreen(
        uiState = uiState,
    )
}
