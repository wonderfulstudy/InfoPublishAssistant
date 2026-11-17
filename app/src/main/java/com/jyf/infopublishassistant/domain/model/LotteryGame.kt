package com.jyf.infopublishassistant.domain.model

/**
 * 一个号码区的开奖规则
 * @param name 分区名字
 * @param count 分区数字个数
 * @param numberRange 分区数字区间，大乐透前区为1-35后区为1-12
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
    val apiGameNo: String,
    val zones: List<BallZone>,
) {
    DLT(
        displayName = "大乐透",
        apiGameNo = "85",
        zones = listOf(
            BallZone(name = "前区", count = 5, numberRange = 1..35),
            BallZone(name = "后区", count = 2, numberRange = 1..12),
        ),
    ),
    QXC(
        displayName = "七星彩",
        apiGameNo = "04",
        zones = listOf(
            BallZone(name = "前区第一至第三位", count = 3, numberRange = 0..9, positional = true),
            BallZone(name = "前区第四至第六位", count = 3, numberRange = 0..9, positional = true),
            BallZone(name = "后区", count = 1, numberRange = 0..14, positional = true),
        ),
    ),
    PLYX(
        displayName = "排列游戏",
        apiGameNo = "350133",
        zones = listOf(
            BallZone(name = "号码", count = 5, numberRange = 0..9, positional = true),
        ),
    ),
    ;

    /** 每注号码总数，用于列表行的宽度预估。 */
    val totalBallCount: Int get() = zones.sumOf { it.count }

    /** 选择页卡片上的规则摘要，由 [zones] 派生，新增彩种无需另写文案。 */
    val ruleSummary: String
        get() = zones.joinToString(" + ") { zone ->
            "${zone.name} ${zone.numberRange.first}-${zone.numberRange.last} 选 ${zone.count}"
        }
}
