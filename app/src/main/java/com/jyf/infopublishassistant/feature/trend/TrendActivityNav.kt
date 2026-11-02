package com.jyf.infopublishassistant.feature.trend

import com.jyf.infopublishassistant.domain.model.LotteryGame

/** 彩种到其独立走势图 Activity 的映射。新增彩种时在此补一个分支。 */
fun LotteryGame.trendActivityClass(): Class<out BaseTrendActivity> = when (this) {
    LotteryGame.DA_LE_TOU -> DaLeTouTrendActivity::class.java
    LotteryGame.QI_XING_CAI -> QiXingCaiTrendActivity::class.java
    LotteryGame.PAI_LIE -> PaiLieTrendActivity::class.java
}
