package com.jyf.infopublishassistant.domain.model

/**
 * 一个号码区的开奖规则，例如大乐透前区「35 选 5」。
 *
 * @param positional true 表示按位置的数字玩法（七星彩 / 排列三 / 排列五）：
 *   号码可重复、且位置本身有意义，因此不做排序；
 *   false 表示组合玩法（大乐透 / 双色球）：号码不重复，习惯上按升序展示。
 */
data class BallZone(
    val name: String,
    val count: Int,
    val numberRange: IntRange,
    val positional: Boolean = false,
)

/**
 * 支持的彩种。新增彩种只需在此追加一个枚举项，
 * 走势页面的列结构由 [zones] 数据驱动，无需改动 UI 代码。
 */
enum class LotteryGame(
    val displayName: String,
    val ruleSummary: String,
    val zones: List<BallZone>,
) {
    DA_LE_TOU(
        displayName = "大乐透",
        ruleSummary = "前区 35 选 5 · 后区 12 选 2",
        zones = listOf(
            BallZone(name = "前区", count = 5, numberRange = 1..35),
            BallZone(name = "后区", count = 2, numberRange = 1..12),
        ),
    ),
    QI_XING_CAI(
        displayName = "七星彩",
        ruleSummary = "前区 6 位 0-9 · 后区 1 位 0-14",
        zones = listOf(
            BallZone(name = "前区", count = 6, numberRange = 0..9, positional = true),
            BallZone(name = "后区", count = 1, numberRange = 0..14, positional = true),
        ),
    ),
    PAI_LIE(
        displayName = "排列游戏",
        ruleSummary = "5 位数字 0-9 · 排列三取前 3 位",
        zones = listOf(
            BallZone(name = "号码", count = 5, numberRange = 0..9, positional = true),
        ),
    ),
    ;

    /** 每注号码总数，用于列表行的宽度预估。 */
    val totalBallCount: Int get() = zones.sumOf { it.count }
}
