package com.jyf.infopublishassistant.feature.trend

import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 锁定各彩种走势图的列结构：分区区间、是否连线、取号逻辑。
 * 这些结构写错不会崩溃，但走势图会悄悄画错列，因此用测试钉住。
 */
class TrendLayoutTest {

    @Test
    fun `line is skipped when the two draws differ by one or are equal`() {
        assertFalse(shouldConnectTrendLine(current = 5, other = 4))
        assertFalse(shouldConnectTrendLine(current = 5, other = 6))
        assertFalse("两期号码相同时不画线", shouldConnectTrendLine(current = 5, other = 5))
        assertTrue(shouldConnectTrendLine(current = 5, other = 3))
        assertTrue(shouldConnectTrendLine(current = 0, other = 9))
    }

    @Test
    fun `da le tou splits the front zone into five ranges and keeps the back zone whole`() {
        val layout = LotteryGame.DLT.trendLayout()
        assertFalse(layout.showPlainNumbers)
        assertEquals(
            listOf(1..7, 8..14, 15..21, 22..28, 29..35, 1..12),
            layout.groups.map { it.range },
        )
        assertTrue(layout.groups.none { it.connectLines })
        // 只有后区要求红色号码
        layout.groups.take(5).forEach { assertNull(it.textColor) }
        assertNotNull(layout.groups.last().textColor)

        val draw = DrawResult(drawNum = "26130", drawResult = listOf(listOf(3, 11, 19, 27, 34), listOf(5, 12)))
        assertEquals(
            listOf(listOf(3), listOf(11), listOf(19), listOf(27), listOf(34), listOf(5, 12)),
            layout.groups.map { it.values(draw) },
        )
    }

    @Test
    fun `qi xing cai has one group per zone and connects the back zone`() {
        val layout = LotteryGame.QXC.trendLayout()
        assertTrue(layout.showPlainNumbers)
        assertEquals(listOf(0..9, 0..9, 0..14), layout.groups.map { it.range })
        assertEquals(listOf(false, false, true), layout.groups.map { it.connectLines })

        val draw = DrawResult(drawNum = "26130", drawResult = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(10)))
        assertEquals(
            listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(10)),
            layout.groups.map { it.values(draw) },
        )
    }

    @Test
    fun `pai lie has one connected group per position`() {
        val layout = LotteryGame.PLYX.trendLayout()
        assertTrue(layout.showPlainNumbers)
        assertEquals(5, layout.groups.size)
        assertTrue(layout.groups.all { it.range == 0..9 && it.connectLines })

        val draw = DrawResult(drawNum = "26130", drawResult = listOf(listOf(4, 7, 3, 8, 6)))
        assertEquals(
            listOf(listOf(4), listOf(7), listOf(3), listOf(8), listOf(6)),
            layout.groups.map { it.values(draw) },
        )
    }

    @Test
    fun `missing zones render as empty marks instead of crashing`() {
        LotteryGame.entries.forEach { game ->
            val layout = game.trendLayout()
            val empty = DrawResult(drawNum = "26130", drawResult = emptyList())
            layout.groups.forEach { group -> assertEquals(emptyList<Int>(), group.values(empty)) }
        }
    }
}
