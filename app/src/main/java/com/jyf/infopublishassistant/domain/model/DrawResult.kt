package com.jyf.infopublishassistant.domain.model

/**
 * 一期开奖结果。
 *
 * @param issue 期号，例如 "26130"。同一彩种的期号按数值大小即为时间先后顺序
 *   （年份前缀占高位），因此排序与分页都按数值比较。
 * @param numbers 按 [LotteryGame.zones] 顺序分组的开奖号码。
 *   组合玩法内已升序排列；位置玩法保持开奖顺序、可含重复数字。
 */
data class DrawResult(
    val issue: String,
    val numbers: List<List<Int>>,
)
