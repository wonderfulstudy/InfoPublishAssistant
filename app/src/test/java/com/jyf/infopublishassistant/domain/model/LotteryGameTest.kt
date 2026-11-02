package com.jyf.infopublishassistant.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 锁定各彩种的开奖规则。
 *
 * 这些数字写错不会导致崩溃，只会让走势图悄悄显示错误的号码结构，
 * 所以用测试把它们钉住，改动规则时必须显式改测试。
 */
class LotteryGameTest {

    @Test
    fun `da le tou is 5 of 35 plus 2 of 12`() {
        val zones = LotteryGame.DA_LE_TOU.zones
        assertEquals(2, zones.size)
        assertEquals(5, zones[0].count)
        assertEquals(1..35, zones[0].numberRange)
        assertEquals(2, zones[1].count)
        assertEquals(1..12, zones[1].numberRange)
        assertTrue(zones.none { it.positional })
    }

    @Test
    fun `qi xing cai is 6 positional digits plus 1 back digit 0 to 14`() {
        val zones = LotteryGame.QI_XING_CAI.zones
        assertEquals(2, zones.size)
        assertEquals(6, zones[0].count)
        assertEquals(0..9, zones[0].numberRange)
        assertTrue(zones[0].positional)
        assertEquals(1, zones[1].count)
        assertEquals(0..14, zones[1].numberRange)
        assertTrue(zones[1].positional)
    }

    @Test
    fun `pai lie is 5 positional digits`() {
        val zones = LotteryGame.PAI_LIE.zones
        assertEquals(1, zones.size)
        assertEquals(5, zones[0].count)
        assertEquals(0..9, zones[0].numberRange)
        assertTrue(zones[0].positional)
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
            setOf("大乐透", "七星彩", "排列"),
            LotteryGame.entries.map { it.displayName }.toSet(),
        )
    }
}
