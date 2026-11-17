package com.jyf.infopublishassistant.feature.trend

import com.jyf.infopublishassistant.domain.model.LotteryGame

/** 大乐透走势图。前区 35 选 5 + 后区 12 选 2。 */
class DLTTrendActivity : BaseTrendActivity() {
    override val game: LotteryGame = LotteryGame.DLT
}
