package com.jyf.infopublishassistant.feature.trend

import com.jyf.infopublishassistant.domain.model.LotteryGame

/** 排列走势图。排列三/排列五合并后的彩种：每期 5 位数字，排列三即前 3 位。 */
class PLYXTrendActivity : BaseTrendActivity() {
    override val game: LotteryGame = LotteryGame.PLYX
}
