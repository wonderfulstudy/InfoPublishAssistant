package com.jyf.infopublishassistant.feature.trend

import androidx.compose.ui.graphics.Color
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.ui.theme.TrendBackZoneRed
import kotlin.math.abs

/**
 * 走势图上的一个分区列。
 *
 * 分区宽度按 [range] 的取值个数等分为子列，开奖号码落在数值对应的子列里，
 * 因此「号码的横向位置」本身就携带数值信息，连接线才有意义。
 *
 * @param range 分区取值范围（含两端）
 * @param values 某期开奖在该分区要标记的号码；渲染时自动去重
 * @param connectLines 是否把相邻两期的号码用线条连起来；差值为 1 时不连
 * @param textColor 号码与连接线颜色，null 表示跟随页面的 numberColor
 */
data class TrendGroup(
    val range: IntRange,
    val values: (DrawResult) -> List<Int>,
    val connectLines: Boolean = false,
    val textColor: Color? = null,
)

/**
 * 一个彩种的走势图列结构：是否展示纯文本开奖号码列 + 若干分区。
 */
data class TrendLayout(
    val showPlainNumbers: Boolean,
    val groups: List<TrendGroup>,
) {
    /** 全部分区子列总数，用于按屏幕宽度均分每子列宽度。 */
    val totalSlots: Int get() = groups.sumOf { it.range.count() }
}

/** 大乐透前区五分区的区间划分。 */
private val DA_LE_TOU_FRONT_ZONES = listOf(1..7, 8..14, 15..21, 22..28, 29..35)

/** 相邻两期号码差值为 1（前一个或后一个）时不画连接线。 */
fun shouldConnectTrendLine(current: Int, other: Int): Boolean = abs(current - other) != 1

/** 各彩种的走势图列结构。 */
fun LotteryGame.trendLayout(): TrendLayout = when (this) {
    LotteryGame.DA_LE_TOU -> TrendLayout(
        showPlainNumbers = false,
        groups = DA_LE_TOU_FRONT_ZONES.map { zone ->
            TrendGroup(
                range = zone,
                values = { draw -> draw.numbers.getOrElse(0) { emptyList() }.filter { it in zone } },
            )
        } + TrendGroup(
            range = 1..12,
            values = { draw -> draw.numbers.getOrElse(1) { emptyList() } },
            textColor = TrendBackZoneRed,
        ),
    )

    LotteryGame.QI_XING_CAI -> TrendLayout(
        showPlainNumbers = true,
        groups = listOf(
            TrendGroup(
                range = 0..9,
                values = { draw -> draw.numbers.getOrElse(0) { emptyList() }.take(3) },
            ),
            TrendGroup(
                range = 0..9,
                values = { draw -> draw.numbers.getOrElse(0) { emptyList() }.drop(3) },
            ),
            TrendGroup(
                range = 0..14,
                values = { draw -> draw.numbers.getOrElse(1) { emptyList() } },
                connectLines = true,
            ),
        ),
    )

    LotteryGame.PAI_LIE -> TrendLayout(
        showPlainNumbers = true,
        groups = (0 until 5).map { position ->
            TrendGroup(
                range = 0..9,
                values = { draw -> draw.numbers.getOrElse(0) { emptyList() }.drop(position).take(1) },
                connectLines = true,
            )
        },
    )
}
