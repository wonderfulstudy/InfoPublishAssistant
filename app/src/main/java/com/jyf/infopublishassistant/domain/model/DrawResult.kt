package com.jyf.infopublishassistant.domain.model

/**
 * 一期开奖结果。
 *
 * @param drawNum 开奖期号
 * @param drawResult 当期开奖结果，按 [LotteryGame.zones] 顺序分组的开奖号码。
 *   组合玩法内已升序排列；位置玩法保持开奖顺序、可含重复数字。
 */
data class DrawResult(
    val drawNum: String,
    val drawResult: List<List<Int>>,
)
