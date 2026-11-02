package com.jyf.infopublishassistant.data.remote

import com.jyf.infopublishassistant.domain.model.LotteryGame
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleLotteryRemoteDataSourceTest {

    private val dataSource = SampleLotteryRemoteDataSource()

    @Test
    fun `every draw obeys its game rules`() = runTest {
        LotteryGame.entries.forEach { game ->
            val draws = dataSource.fetchDrawHistory(game, page = 1, pageSize = 50)
            assertEquals(50, draws.size)

            draws.forEach { draw ->
                assertEquals(
                    "${game.displayName} 号码区数量不符",
                    game.zones.size,
                    draw.numbers.size,
                )
                game.zones.forEachIndexed { zoneIndex, zone ->
                    val numbers = draw.numbers[zoneIndex]
                    assertEquals(zone.count, numbers.size)
                    numbers.forEach { number ->
                        assertTrue(
                            "${game.displayName} 第 $zoneIndex 区号码 $number 超出 ${zone.numberRange}",
                            number in zone.numberRange,
                        )
                    }
                    if (!zone.positional) {
                        // 组合玩法：不重复且升序，与官方展示习惯一致
                        assertEquals(numbers.sorted(), numbers)
                        assertEquals(numbers.size, numbers.distinct().size)
                    }
                }
            }
        }
    }

    @Test
    fun `positional games may repeat digits and keep their order`() = runTest {
        // 排列、七星彩是按位玩法，如果实现里误加了排序或去重，
        // 走势数据就全错了。这里用足够多的期数确保确实出现过重复数字。
        val draws = dataSource.fetchDrawHistory(LotteryGame.PAI_LIE, page = 1, pageSize = 200)
        val hasRepeat = draws.any { draw -> draw.numbers[0].distinct().size < draw.numbers[0].size }
        assertTrue("200 期排列里应该出现过重复数字", hasRepeat)

        val hasUnsorted = draws.any { draw -> draw.numbers[0] != draw.numbers[0].sorted() }
        assertTrue("位置玩法不应被排序", hasUnsorted)
    }

    @Test
    fun `issues strictly descend within and across pages`() = runTest {
        val issues = (1..5).flatMap { page ->
            dataSource.fetchDrawHistory(LotteryGame.DA_LE_TOU, page, 20).map { it.issue }
        }
        assertEquals("跨页不应出现重复期号", issues.size, issues.distinct().size)

        // 期号按数值比较即为时间倒序，跨年借位（26001 → 25150）后依然成立
        val numeric = issues.map { it.toInt() }
        assertEquals(numeric.sortedDescending(), numeric)
    }

    @Test
    fun `issue numbering borrows correctly across the year boundary`() = runTest {
        val draws = dataSource.fetchDrawHistory(LotteryGame.DA_LE_TOU, page = 1, pageSize = 300)
        val issues = draws.map { it.issue }
        assertTrue("应该包含最新一期 26130", issues.contains("26130"))
        // 第 131 期往前就跨到上一年，年份前缀必须递减而不是变成 26000
        assertTrue("跨年后应进入 25 年", issues.any { it.startsWith("25") })
        assertTrue(issues.none { it.endsWith("000") })
    }

    @Test
    fun `a page past the sample range is empty`() = runTest {
        // 样本共 300 期，pageSize 20 时第 15 页是最后一页
        assertEquals(20, dataSource.fetchDrawHistory(LotteryGame.DA_LE_TOU, 15, 20).size)
        assertTrue(dataSource.fetchDrawHistory(LotteryGame.DA_LE_TOU, 16, 20).isEmpty())
    }

    @Test
    fun `a partial last page returns fewer rows than pageSize`() = runTest {
        // 300 / 7 = 42 余 6，所以第 43 页只剩 6 条
        val draws = dataSource.fetchDrawHistory(LotteryGame.DA_LE_TOU, page = 43, pageSize = 7)
        assertEquals(6, draws.size)
    }

    @Test
    fun `the same request is deterministic`() = runTest {
        // 稳定性很重要：翻页、退出重进、缓存命中后展示的数据必须一致，否则无法对照调试
        val first = dataSource.fetchDrawHistory(LotteryGame.QI_XING_CAI, page = 3, pageSize = 20)
        val second = dataSource.fetchDrawHistory(LotteryGame.QI_XING_CAI, page = 3, pageSize = 20)
        assertEquals(first, second)
    }

    @Test
    fun `rejects a non positive page or page size`() {
        // require 在任何挂起点之前就会抛出，因此这里用 runBlocking 直接断言即可
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { dataSource.fetchDrawHistory(LotteryGame.DA_LE_TOU, page = 0, pageSize = 20) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { dataSource.fetchDrawHistory(LotteryGame.DA_LE_TOU, page = 1, pageSize = 0) }
        }
    }
}
