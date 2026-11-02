package com.jyf.infopublishassistant.core.display

import org.junit.Assert.assertEquals
import org.junit.Test

class DpadRemapperTest {

    @Test
    fun `rotation 0 keeps every direction unchanged`() {
        DpadDirection.entries.forEach { direction ->
            assertEquals(direction, DpadRemapper.remap(direction, ScreenRotation.ROTATION_0))
        }
    }

    @Test
    fun `rotation 90 maps screen directions into content space`() {
        // 内容顺时针转 90° 后，页面顶部指向屏幕右侧
        assertEquals(DpadDirection.LEFT, DpadRemapper.remap(DpadDirection.UP, ScreenRotation.ROTATION_90))
        assertEquals(DpadDirection.RIGHT, DpadRemapper.remap(DpadDirection.DOWN, ScreenRotation.ROTATION_90))
        assertEquals(DpadDirection.DOWN, DpadRemapper.remap(DpadDirection.LEFT, ScreenRotation.ROTATION_90))
        assertEquals(DpadDirection.UP, DpadRemapper.remap(DpadDirection.RIGHT, ScreenRotation.ROTATION_90))
    }

    @Test
    fun `rotation 180 swaps opposite directions`() {
        assertEquals(DpadDirection.DOWN, DpadRemapper.remap(DpadDirection.UP, ScreenRotation.ROTATION_180))
        assertEquals(DpadDirection.UP, DpadRemapper.remap(DpadDirection.DOWN, ScreenRotation.ROTATION_180))
        assertEquals(DpadDirection.RIGHT, DpadRemapper.remap(DpadDirection.LEFT, ScreenRotation.ROTATION_180))
        assertEquals(DpadDirection.LEFT, DpadRemapper.remap(DpadDirection.RIGHT, ScreenRotation.ROTATION_180))
    }

    @Test
    fun `rotation 270 maps screen directions into content space`() {
        assertEquals(DpadDirection.RIGHT, DpadRemapper.remap(DpadDirection.UP, ScreenRotation.ROTATION_270))
        assertEquals(DpadDirection.LEFT, DpadRemapper.remap(DpadDirection.DOWN, ScreenRotation.ROTATION_270))
        assertEquals(DpadDirection.UP, DpadRemapper.remap(DpadDirection.LEFT, ScreenRotation.ROTATION_270))
        assertEquals(DpadDirection.DOWN, DpadRemapper.remap(DpadDirection.RIGHT, ScreenRotation.ROTATION_270))
    }

    @Test
    fun `90 and 270 are inverse mappings`() {
        // 转 90° 再转 270° 等于没转：两次换算必须互相抵消，
        // 这条能抓住「某个角度映射写反」的错误，而单个角度的断言抓不住
        DpadDirection.entries.forEach { direction ->
            val once = DpadRemapper.remap(direction, ScreenRotation.ROTATION_90)
            val twice = DpadRemapper.remap(once, ScreenRotation.ROTATION_270)
            assertEquals(direction, twice)
        }
    }

    @Test
    fun `applying 90 four times returns to the original direction`() {
        DpadDirection.entries.forEach { direction ->
            var current = direction
            repeat(4) { current = DpadRemapper.remap(current, ScreenRotation.ROTATION_90) }
            assertEquals(direction, current)
        }
    }

    @Test
    fun `every rotation produces a bijection over the four directions`() {
        // 映射必须是双射，否则会出现「两个物理键指向同一内容方向、某个方向永远到不了」
        ScreenRotation.entries.forEach { rotation ->
            val mapped = DpadDirection.entries.map { DpadRemapper.remap(it, rotation) }
            assertEquals(4, mapped.distinct().size)
        }
    }
}
