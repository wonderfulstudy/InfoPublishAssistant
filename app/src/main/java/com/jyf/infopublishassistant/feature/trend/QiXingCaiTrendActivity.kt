package com.jyf.infopublishassistant.feature.trend

import com.jyf.infopublishassistant.domain.model.LotteryGame

/** 七星彩走势图。7 位数字 0-9，按位置展示、可重复。 */
class QiXingCaiTrendActivity : BaseTrendActivity() {
    override val game: LotteryGame = LotteryGame.QI_XING_CAI
}
