package com.jyf.infopublishassistant.core.display

import android.view.KeyEvent

/**
 * 遥控器十字键方向。
 *
 * 刻意与 [KeyEvent] 的 keyCode 解耦：方向换算是这个应用里最容易算错、
 * 出错后又最难在真机上定位的纯逻辑，拆出来才能在 JVM 单测里直接覆盖。
 */
enum class DpadDirection { UP, DOWN, LEFT, RIGHT }

/**
 * 把遥控器方向键换算到旋转后的内容坐标系。
 *
 * 内容被顺时针旋转 [ScreenRotation.degrees] 后，物理按键方向与内容方向不再一致。
 * 例：内容顺时针转 90° 时，页面顶部指向屏幕右侧，用户按「上」，
 * 在内容坐标系里其实是要往「左」走。
 *
 * 推导：屏幕坐标 y 轴向下、正角度为顺时针。设屏幕方向向量 d_screen，
 * 则内容方向 d_content = R(-θ) · d_screen。
 *
 * θ=90° 时 R(-90°) 把 (x, y) 映射为 (y, -x)：
 *   屏幕上 (0,-1) → 内容左 (-1, 0)  ⇒ UP    发 LEFT
 *   屏幕右 (1, 0) → 内容上 (0,-1)   ⇒ RIGHT 发 UP
 *   屏幕下 (0, 1) → 内容右 (1, 0)   ⇒ DOWN  发 RIGHT
 *   屏幕左 (-1,0) → 内容下 (0, 1)   ⇒ LEFT  发 DOWN
 *
 * ⚠️ 待在真机上确认：Compose 的 FocusFinder 做方向搜索时，可能已经把 graphicsLayer
 * 的旋转变换计入 boundsInWindow。若确实如此，本重映射会造成「双重修正」，焦点反而走歪。
 * 因此该行为由设置项「方向键跟随画面旋转」控制，实测后如方向相反，
 * 把 [DisplaySettings.dpadFollowsRotation] 的默认值改成 false 即可。
 */
object DpadRemapper {

    /** 纯逻辑换算，可在 JVM 单测中直接验证。 */
    fun remap(direction: DpadDirection, rotation: ScreenRotation): DpadDirection =
        when (rotation) {
            ScreenRotation.ROTATION_0 -> direction

            ScreenRotation.ROTATION_90 -> when (direction) {
                DpadDirection.UP -> DpadDirection.LEFT
                DpadDirection.DOWN -> DpadDirection.RIGHT
                DpadDirection.LEFT -> DpadDirection.DOWN
                DpadDirection.RIGHT -> DpadDirection.UP
            }

            ScreenRotation.ROTATION_180 -> when (direction) {
                DpadDirection.UP -> DpadDirection.DOWN
                DpadDirection.DOWN -> DpadDirection.UP
                DpadDirection.LEFT -> DpadDirection.RIGHT
                DpadDirection.RIGHT -> DpadDirection.LEFT
            }

            ScreenRotation.ROTATION_270 -> when (direction) {
                DpadDirection.UP -> DpadDirection.RIGHT
                DpadDirection.DOWN -> DpadDirection.LEFT
                DpadDirection.LEFT -> DpadDirection.UP
                DpadDirection.RIGHT -> DpadDirection.DOWN
            }
        }

    /** Android 适配层：非方向键（确认键、返回键、菜单键等）原样返回。 */
    fun remapKeyCode(keyCode: Int, rotation: ScreenRotation): Int {
        if (rotation == ScreenRotation.ROTATION_0) return keyCode
        val direction = keyCode.toDpadDirection() ?: return keyCode
        return remap(direction, rotation).toKeyCode()
    }

    private fun Int.toDpadDirection(): DpadDirection? = when (this) {
        KeyEvent.KEYCODE_DPAD_UP -> DpadDirection.UP
        KeyEvent.KEYCODE_DPAD_DOWN -> DpadDirection.DOWN
        KeyEvent.KEYCODE_DPAD_LEFT -> DpadDirection.LEFT
        KeyEvent.KEYCODE_DPAD_RIGHT -> DpadDirection.RIGHT
        else -> null
    }

    private fun DpadDirection.toKeyCode(): Int = when (this) {
        DpadDirection.UP -> KeyEvent.KEYCODE_DPAD_UP
        DpadDirection.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
        DpadDirection.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
        DpadDirection.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
    }

    /**
     * 复制一份只替换 keyCode 的事件，其余字段（downTime / deviceId / source 等）全部保留，
     * 否则部分电视的焦点框架会因为事件不完整而丢弃按键。
     */
    fun KeyEvent.withKeyCode(newKeyCode: Int): KeyEvent = KeyEvent(
        downTime,
        eventTime,
        action,
        newKeyCode,
        repeatCount,
        metaState,
        deviceId,
        scanCode,
        flags,
        source,
    )
}
