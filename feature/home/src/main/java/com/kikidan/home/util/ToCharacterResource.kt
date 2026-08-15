package com.kikidan.home.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.kikidan.designsystem.R


@Composable
fun Int.toCharacterPainter() = when {
        this <= 35 -> painterResource(R.drawable.img_todak_mood_level01)
        this <= 65 -> painterResource(R.drawable.img_todak_mood_level02)
        this <= 80 -> painterResource(R.drawable.img_todak_mood_level03)
        else -> painterResource(R.drawable.img_todak_mood_level04)
    }
