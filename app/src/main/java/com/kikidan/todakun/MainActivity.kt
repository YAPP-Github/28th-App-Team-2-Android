package com.kikidan.todakun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.repository.TokenRepository
import com.kikidan.mypage.setting.ui.AppSettingWithdrawalNoticeRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenRepository: TokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // TODO: 실 서버 API 수동 테스트용 임시 토큰 주입. 로그인 플로우 완성되면 제거.
        runBlocking {
            tokenRepository.saveToken(AuthToken(accessToken = TEMP_ACCESS_TOKEN, refreshToken = ""))
        }
        setContent {
            TodakunTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    AppSettingWithdrawalNoticeRoute(
                        onNavigateBack = { finish() },
                        reason = stringArrayResource(R.array.wheel_picker_withdrawal_reasons)[0],
                        detailReason = "",
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    companion object {
        private const val TEMP_ACCESS_TOKEN =
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwMTlmY2QxMi03MDc0LTc0NDMtYmExYy1hZWMyYjVlY2MzNTMiLCJqdGkiOiIwMWEwMTQxYy1lYWI1LTc0NmMtOGE1Yy1jMDk2ZjdkZjA0MTMiLCJhZG1pbiI6dHJ1ZSwiaWF0IjoxNzg3MDQzODM0LCJleHAiOjE3ODc2NDg2MzR9.IAbqi6ZkiIGqng89pnfVy0flbA5HM6xmR1wTQBBJnwXpagWum35i2diel0xyohA3-4HRA3oqAyZCIj901w6qNA"
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    TodakunTheme {
        Greeting("Android")
    }
}
