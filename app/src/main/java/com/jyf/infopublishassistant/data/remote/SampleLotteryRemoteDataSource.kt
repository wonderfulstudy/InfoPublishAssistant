package com.jyf.infopublishassistant.data.remote

import com.jyf.infopublishassistant.domain.model.BallZone
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import java.util.Random

/**
 * 示例数据源：生成符合各彩种规则的确定性假数据，让 App 在真实接口接入前就能完整跑通。
 *
 * TODO(接口实现): 用你的真实网络实现替换 [com.jyf.infopublishassistant.core.platform.AppContainer]
 *   中对本类的绑定。本类可以直接删除。
 *
 * 同一期号永远生成同一组号码（以 issue + game 作为随机种子），
 * 因此翻页、重复进入页面、缓存命中后的展示都是稳定的，便于对照调试。
 */
class SampleLotteryRemoteDataSource : LotteryRemoteDataSource {

    override suspend fun fetchDrawHistory(
        game: LotteryGame,
        page: Int,
        pageSize: Int,
    ): List<DrawResult> {
        require(page >= 1) { "page 必须从 1 开始，当前为 $page" }
        require(pageSize > 0) { "pageSize 必须为正数，当前为 $pageSize" }

        val startIndex = (page - 1) * pageSize
        if (startIndex >= TOTAL_ISSUES) return emptyList()
        val endIndex = minOf(startIndex + pageSize, TOTAL_ISSUES)

        return (startIndex until endIndex).map { offset ->
            val issue = issueBeforeLatest(offset)
            DrawResult(issue = issue, numbers = game.zones.map { zone -> drawZone(zone, issue, game) })
        }
    }

    /** 最新一期为 [LATEST_ISSUE]，往前推 [offset] 期，跨年时自动借位。 */
    private fun issueBeforeLatest(offset: Int): String {
        var year = LATEST_YEAR
        var sequence = LATEST_SEQUENCE - offset
        while (sequence < 1) {
            year -= 1
            sequence += ISSUES_PER_YEAR
        }
        return "%02d%03d".format(year, sequence)
    }

    private fun drawZone(zone: BallZone, issue: String, game: LotteryGame): List<Int> {
        val random = Random(issue.hashCode().toLong() * 31 + game.ordinal)
        val span = zone.numberRange.last - zone.numberRange.first + 1
        return if (zone.positional) {
            // 位置玩法：各位独立取值，可重复，保持顺序
            List(zone.count) { zone.numberRange.first + random.nextInt(span) }
        } else {
            // 组合玩法：不重复抽样后升序
            val pool = zone.numberRange.toMutableList()
            List(zone.count) { pool.removeAt(random.nextInt(pool.size)) }.sorted()
        }
    }

    private companion object {
        const val TOTAL_ISSUES = 300
        const val LATEST_YEAR = 26
        const val LATEST_SEQUENCE = 130
        const val ISSUES_PER_YEAR = 150
    }
}
