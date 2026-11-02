package com.jyf.infopublishassistant.core.display

/**
 * 画面旋转角度。按 0° → 90° → 180° → 270° → 0° 循环切换。
 *
 * 这里指的是「内容层旋转」：窗口方向保持不变，由 [RotatedRoot] 把整棵 Compose 树
 * 旋转并交换宽高。之所以不用 `Activity.requestedOrientation`：
 * 1. 大量电视/盒子固件会忽略 screenOrientation，系统方向切换未必生效；
 * 2. 内容层旋转不触发 configuration change，Activity 不重建，
 *    走势图的滚动位置与焦点天然保留。
 */
enum class ScreenRotation(val degrees: Float) {
    ROTATION_0(0f),
    ROTATION_90(90f),
    ROTATION_180(180f),
    ROTATION_270(270f),
    ;

    fun next(): ScreenRotation = entries[(ordinal + 1) % entries.size]

    /** 90° / 270° 时内容宽高需要与窗口宽高互换。 */
    val swapsContentSize: Boolean
        get() = this == ROTATION_90 || this == ROTATION_270

    companion object {
        val DEFAULT = ROTATION_0

        fun fromName(value: String?): ScreenRotation =
            entries.firstOrNull { it.name == value } ?: DEFAULT
    }
}
