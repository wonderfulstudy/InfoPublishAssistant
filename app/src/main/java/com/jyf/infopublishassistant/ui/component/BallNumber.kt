package com.jyf.infopublishassistant.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jyf.infopublishassistant.domain.model.BallZone
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.ui.theme.DrawNumberTextColor
import com.jyf.infopublishassistant.ui.theme.TextSecondary

/**
 * 单个号码。
 *
 * 文字颜色一律显式指定（[color]）：TV Material3 的默认 content color 由外层 Surface 提供，
 * 依赖环境色会在某些容器里变成不可见的深色字。
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BallNumber(
    number: Int,
    zone: BallZone,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    color: Color = DrawNumberTextColor,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = color,
            fontSize = (size.value * 0.47f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 一期的完整开奖号码，按 [LotteryGame.zones] 分区渲染，区间用「+」分隔。
 *
 * 例如大乐透显示为 `3 11 19 27 34 + 5 12`，排列三显示为 `3 7 1`。
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DrawNumbers(
    game: LotteryGame,
    draw: DrawResult,
    modifier: Modifier = Modifier,
    ballSize: Dp = 34.dp,
    numberColor: Color = DrawNumberTextColor,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        game.zones.forEachIndexed { zoneIndex, zone ->
            if (zoneIndex > 0) {
                Text(
                    text = "+",
                    color = TextSecondary,
                    fontSize = 18.sp,
                )
            }
            // getOrElse：远端数据可能缺区或长度不符，界面层按空处理而不是崩溃
            val zoneNumbers = draw.drawResult.getOrElse(zoneIndex) { emptyList() }
            zoneNumbers.forEach { number ->
                BallNumber(
                    number = number,
                    zone = zone,
                    size = ballSize,
                    color = numberColor,
                )
            }
        }
    }
}
