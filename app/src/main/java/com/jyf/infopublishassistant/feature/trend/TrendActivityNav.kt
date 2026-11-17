package com.jyf.infopublishassistant.feature.trend

import com.jyf.infopublishassistant.domain.model.LotteryGame

/** 彩种到其独立走势图 Activity 的映射。新增彩种时在此补一个分支。 */
fun LotteryGame.trendActivityClass(): Class<out BaseTrendActivity> = when (this) {
    LotteryGame.DLT -> DLTTrendActivity::class.java
    LotteryGame.QXC -> QXCTrendActivity::class.java
    LotteryGame.PLYX -> PLYXTrendActivity::class.java
}
