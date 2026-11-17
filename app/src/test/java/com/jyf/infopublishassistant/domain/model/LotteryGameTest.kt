package com.jyf.infopublishassistant.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 锁定各彩种的开奖规则与接口编号。
 *
 * 这些数字写错不会导致崩溃，只会让走势图悄悄显示错误的号码结构、
 * 或请求到错误彩种的历史数据，所以用测试把它们钉住，改动时必须显式改测试。
 */
class LotteryGameTest {

    @Test
    fun `da le tou is 5 of 35 plus 2 of 12`() {
        val zones = LotteryGame.DLT.zones
        assertEquals(2, zones.size)
        assertEquals(5, zones[0].count)
        assertEquals(1..35, zones[0].numberRange)
        assertEquals(2, zones[1].count)
        assertEquals(1..12, zones[1].numberRange)
        assertTrue(zones.none { it.positional })
    }

    @Test
    fun `qi xing cai splits six positional digits into two groups plus 1 back digit`() {
        val zones = LotteryGame.QXC.zones
        assertEquals(3, zones.size)
        assertEquals(3, zones[0].count)
        assertEquals(0..9, zones[0].numberRange)
        assertTrue(zones[0].positional)
        assertEquals(3, zones[1].count)
        assertEquals(0..9, zones[1].numberRange)
        assertTrue(zones[1].positional)
        assertEquals(1, zones[2].count)
        assertEquals(0..14, zones[2].numberRange)
        assertTrue(zones[2].positional)
    }

    @Test
    fun `pai lie is 5 positional digits`() {
        val zones = LotteryGame.PLYX.zones
        assertEquals(1, zones.size)
        assertEquals(5, zones[0].count)
        assertEquals(0..9, zones[0].numberRange)
        assertTrue(zones[0].positional)
    }

    @Test
    fun `each game maps to its sporttery gameNo`() {
        assertEquals("85", LotteryGame.DLT.apiGameNo)
        assertEquals("04", LotteryGame.QXC.apiGameNo)
        assertEquals("350133", LotteryGame.PLYX.apiGameNo)
    }

    @Test
    fun `every game exposes a display name and rule summary`() {
        LotteryGame.entries.forEach { game ->
            assertTrue(game.displayName.isNotBlank())
            assertTrue(game.ruleSummary.isNotBlank())
        }
    }

    @Test
    fun `total ball count matches the sum of all zones`() {
        LotteryGame.entries.forEach { game ->
            assertEquals(game.zones.sumOf { it.count }, game.totalBallCount)
        }
    }

    @Test
    fun `all three requested games are present`() {
        assertEquals(
            setOf("大乐透", "七星彩", "排列游戏"),
            LotteryGame.entries.map { it.displayName }.toSet(),
        )
    }
}
