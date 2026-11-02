package com.jyf.infopublishassistant.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ---- 电视亮色场景专用配色 ----
// 实机屏幕上深色底发灰、对比不足，因此整体改用亮色底 + 深色字，
// 分区背景用低饱和浅色保证相邻分区可区分又不抢号码的颜色。

val ScreenBackground = Color(0xFFF7F7F5)
val SurfaceBackground = Color(0xFFFFFFFF)
val SurfaceBackgroundAlt = Color(0xFFEBEBE6)

val FocusBorder = Color(0xFF1A56DB)
val FocusBackground = Color(0x1F1A56DB)
val IdleBorder = Color(0x14000000)

val TextPrimary = Color(0xFF17171C)
val TextSecondary = Color(0xFF5A5A64)

// 走势图两类文字的颜色，改这里即可全局生效；
// 也可以在 TrendScreen 的 issueColor / numberColor 参数上按页面单独覆盖。
val IssueTextColor = TextSecondary
val DrawNumberTextColor = TextPrimary

// 走势图分区背景：低饱和浅色，按分区顺序循环取色，保证相邻分区不同色。
val TrendZoneBackgrounds = listOf(
    Color(0xFFE3EFDD),
    Color(0xFFFFFFFF),
    Color(0xFFE1EAF6),
    Color(0xFFF7EEDF),
    Color(0xFFF2E4EE),
    Color(0xFFE0EEEC),
)

// 大乐透后区号码按需求固定用红色。
val TrendBackZoneRed = Color(0xFFD93025)
