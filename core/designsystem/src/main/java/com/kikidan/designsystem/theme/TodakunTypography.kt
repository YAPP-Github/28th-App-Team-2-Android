package com.kikidan.designsystem.theme

import androidx.compose.runtime.Immutable
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
object TodakunTypography {
    // Heading 1
    val heading1ExtraBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 44.sp,
        )
    val heading1Bold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 44.sp,
        )
    val heading1SemiBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 44.sp,
        )

    // Heading 2
    val heading2ExtraBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            lineHeight = 38.sp,
        )
    val heading2Bold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 38.sp,
        )
    val heading2SemiBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 38.sp,
        )

    // Heading 3
    val heading3Bold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        )
    val heading3Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        )
    val heading3Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        )

    // Heading 4
    val heading4Bold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 30.sp,
        )
    val heading4Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 30.sp,
        )
    val heading4Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 30.sp,
        )

    // Body 1
    val body1Bold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 26.sp,
        )
    val body1Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 26.sp,
        )
    val body1Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 26.sp,
        )

    // Body 2
    val body2SemiBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
    val body2Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )
    val body2Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )

    // Body 3
    val body3SemiBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    val body3Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    val body3Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

    // Caption 1
    val caption1SemiBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    val caption1Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    val caption1Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

    // Caption 2
    val caption2SemiBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )
    val caption2Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )
    val caption2Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 14.sp,
        )

    // Caption 3
    val caption3SemiBold: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            lineHeight = 13.sp,
        )
    val caption3Medium: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 13.sp,
        )
    val caption3Regular: TextStyle =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 13.sp,
        )
}
