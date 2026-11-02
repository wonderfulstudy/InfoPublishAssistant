package com.jyf.infopublishassistant.core.display

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

/**
 * 按 [rotation] 旋转整棵内容树。
 *
 * 做法：外层容器铺满窗口，内层容器在 90°/270° 时把宽高互换后居中对齐，
 * 再绕自身中心旋转。因为内层中心与窗口中心重合，旋转后的包围盒恰好等于窗口尺寸，
 * 不会溢出也不会留黑边。
 *
 * 这里必须用 [Modifier.requiredSize] 而不是 `size`：`size` 会被父级约束裁剪，
 * 在 1920x1080 的窗口里要求 1080x1920 会被压成 1080x1080。
 *
 * 旋转是即时切换、不做动画：若加过渡动画，尺寸会在角度变化的同时突变，
 * 中间帧内容会溢出窗口，观感反而更差。
 */
@Composable
fun RotatedRoot(
    rotation: ScreenRotation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (rotation == ScreenRotation.ROTATION_0) {
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = if (rotation.swapsContentSize) maxHeight else maxWidth
        val contentHeight = if (rotation.swapsContentSize) maxWidth else maxHeight

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .requiredSize(contentWidth, contentHeight)
                .rotate(rotation.degrees),
        ) {
            content()
        }
    }
}
