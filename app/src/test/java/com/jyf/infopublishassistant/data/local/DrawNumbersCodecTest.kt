package com.jyf.infopublishassistant.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class DrawNumbersCodecTest {

    @Test
    fun `round trips a multi zone draw`() {
        val numbers = listOf(listOf(3, 11, 19, 27, 34), listOf(5, 12))
        assertEquals(numbers, DrawNumbersCodec.decode(DrawNumbersCodec.encode(numbers)))
    }

    @Test
    fun `round trips a single zone positional draw with repeats`() {
        // 排列三允许重复数字且顺序有意义，编码不能排序或去重
        val numbers = listOf(listOf(7, 7, 3))
        assertEquals(numbers, DrawNumbersCodec.decode(DrawNumbersCodec.encode(numbers)))
    }

    @Test
    fun `round trips a zone containing zero`() {
        val numbers = listOf(listOf(0, 9, 5))
        assertEquals(numbers, DrawNumbersCodec.decode(DrawNumbersCodec.encode(numbers)))
    }

    @Test
    fun `encodes zones with a pipe and numbers with a comma`() {
        // 格式会被写进数据库，锁定它可以防止无意中改动导致老缓存解析失败
        assertEquals(
            "3,11,19,27,34|5,12",
            DrawNumbersCodec.encode(listOf(listOf(3, 11, 19, 27, 34), listOf(5, 12))),
        )
    }

    @Test
    fun `empty list round trips via empty string`() {
        assertEquals("", DrawNumbersCodec.encode(emptyList()))
        assertEquals(emptyList<List<Int>>(), DrawNumbersCodec.decode(""))
    }
}
