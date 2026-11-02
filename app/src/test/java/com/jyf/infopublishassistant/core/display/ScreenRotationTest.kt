package com.jyf.infopublishassistant.core.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenRotationTest {

    @Test
    fun `next cycles 0 to 90 to 180 to 270 and wraps back to 0`() {
        assertEquals(ScreenRotation.ROTATION_90, ScreenRotation.ROTATION_0.next())
        assertEquals(ScreenRotation.ROTATION_180, ScreenRotation.ROTATION_90.next())
        assertEquals(ScreenRotation.ROTATION_270, ScreenRotation.ROTATION_180.next())
        assertEquals(ScreenRotation.ROTATION_0, ScreenRotation.ROTATION_270.next())
    }

    @Test
    fun `degrees match the enum names`() {
        assertEquals(0f, ScreenRotation.ROTATION_0.degrees, 0f)
        assertEquals(90f, ScreenRotation.ROTATION_90.degrees, 0f)
        assertEquals(180f, ScreenRotation.ROTATION_180.degrees, 0f)
        assertEquals(270f, ScreenRotation.ROTATION_270.degrees, 0f)
    }

    @Test
    fun `only 90 and 270 swap the content size`() {
        // RotatedRoot 依据该标记决定是否交换宽高，判断错了会导致内容被裁掉一半或留黑边
        assertFalse(ScreenRotation.ROTATION_0.swapsContentSize)
        assertTrue(ScreenRotation.ROTATION_90.swapsContentSize)
        assertFalse(ScreenRotation.ROTATION_180.swapsContentSize)
        assertTrue(ScreenRotation.ROTATION_270.swapsContentSize)
    }

    @Test
    fun `fromName falls back to default for unknown or null values`() {
        // 持久化的字符串可能来自旧版本或被外部改动，不能让应用起不来
        assertEquals(ScreenRotation.DEFAULT, ScreenRotation.fromName(null))
        assertEquals(ScreenRotation.DEFAULT, ScreenRotation.fromName("ROTATION_45"))
        assertEquals(ScreenRotation.DEFAULT, ScreenRotation.fromName(""))
        assertEquals(ScreenRotation.ROTATION_180, ScreenRotation.fromName("ROTATION_180"))
    }
}
