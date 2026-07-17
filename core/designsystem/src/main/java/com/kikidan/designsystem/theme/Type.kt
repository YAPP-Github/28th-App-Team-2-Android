package com.kikidan.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kikidan.designsystem.R

private val PretendardFontFamily =
    FontFamily(
        Font(R.font.pretendard_extra_bold, FontWeight.ExtraBold),
        Font(R.font.pretendard_bold, FontWeight.Bold),
        Font(R.font.pretendard_semi_bold, FontWeight.SemiBold),
        Font(R.font.pretendard_medium, FontWeight.Medium),
        Font(R.font.pretendard_regular, FontWeight.Normal),
    )

@Immutable
data class TodakunTypography(
    // Heading 1
    val heading1ExtraBold: TextStyle,
    val heading1Bold: TextStyle,
    val heading1SemiBold: TextStyle,
    // Heading 2
    val heading2ExtraBold: TextStyle,
    val heading2Bold: TextStyle,
    val heading2SemiBold: TextStyle,
    // Heading 3
    val heading3Bold: TextStyle,
    val heading3Medium: TextStyle,
    val heading3Regular: TextStyle,
    // Heading 4
    val heading4Bold: TextStyle,
    val heading4Medium: TextStyle,
    val heading4Regular: TextStyle,
    // Body 1
    val body1Bold: TextStyle,
    val body1Medium: TextStyle,
    val body1Regular: TextStyle,
    // Body 2
    val body2SemiBold: TextStyle,
    val body2Medium: TextStyle,
    val body2Regular: TextStyle,
    // Body 3
    val body3SemiBold: TextStyle,
    val body3Medium: TextStyle,
    val body3Regular: TextStyle,
    // Caption 1
    val caption1SemiBold: TextStyle,
    val caption1Medium: TextStyle,
    val caption1Regular: TextStyle,
    // Caption 2
    val caption2SemiBold: TextStyle,
    val caption2Medium: TextStyle,
    val caption2Regular: TextStyle,
    // Caption 3
    val caption3SemiBold: TextStyle,
    val caption3Medium: TextStyle,
    val caption3Regular: TextStyle,
)

internal val defaultTodakunTypography =
    TodakunTypography(
        heading1ExtraBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 41.6.sp,
            ),
        heading1Bold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 41.6.sp,
            ),
        heading1SemiBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 41.6.sp,
            ),
        heading2ExtraBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 36.4.sp,
            ),
        heading2Bold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.4.sp,
            ),
        heading2SemiBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 36.4.sp,
            ),
        heading3Bold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 31.2.sp,
            ),
        heading3Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                lineHeight = 31.2.sp,
            ),
        heading3Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 31.2.sp,
            ),
        heading4Bold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.6.sp,
            ),
        heading4Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 28.6.sp,
            ),
        heading4Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.6.sp,
            ),
        body1Bold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 23.4.sp,
            ),
        body1Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 23.4.sp,
            ),
        body1Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 23.4.sp,
            ),
        body2SemiBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 20.8.sp,
            ),
        body2Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 20.8.sp,
            ),
        body2Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 20.8.sp,
            ),
        body3SemiBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.2.sp,
            ),
        body3Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 18.2.sp,
            ),
        body3Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 18.2.sp,
            ),
        caption1SemiBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 15.6.sp,
            ),
        caption1Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 15.6.sp,
            ),
        caption1Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 15.6.sp,
            ),
        caption2SemiBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                lineHeight = 14.3.sp,
            ),
        caption2Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.3.sp,
            ),
        caption2Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 14.3.sp,
            ),
        caption3SemiBold =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            ),
        caption3Medium =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            ),
        caption3Regular =
            TextStyle(
                fontFamily = PretendardFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = 13.sp,
            ),
    )

val LocalTodakunTypography = staticCompositionLocalOf { defaultTodakunTypography }
