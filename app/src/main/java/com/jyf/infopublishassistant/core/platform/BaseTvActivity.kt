package com.jyf.infopublishassistant.core.platform

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jyf.infopublishassistant.InfoPublishApp
import com.jyf.infopublishassistant.core.display.DpadRemapper
import com.jyf.infopublishassistant.core.display.DpadRemapper.withKeyCode
import com.jyf.infopublishassistant.core.display.RotatedRoot
import com.jyf.infopublishassistant.ui.theme.InfoPublishAssistantTheme

/**
 * 所有电视页面的基类，集中处理三件与业务无关但每页都要做的事：
 *
 * 1. **内容层旋转**：用 [RotatedRoot] 按全局设置旋转整棵 UI 树。因为不改变窗口方向，
 *    所以不会触发 Activity 重建，页面状态（滚动位置、焦点）自动保留。
 * 2. **方向键重映射**：旋转后物理按键方向与内容方向不一致，在事件进入 Compose 之前换算。
 * 3. **常亮**：开奖信息属于长时间展示场景，避免中途息屏。
 */
abstract class BaseTvActivity : ComponentActivity() {

    protected val appContainer: AppContainer
        get() = (application as InfoPublishApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 设置页面内容，自动套上主题与旋转容器。
     * 电视环境一律使用深色主题：浅色背景在大屏上刺眼，且号码球配色是按深色底设计的。
     */
    protected fun setRotatedContent(content: @Composable () -> Unit) {
        setContent {
            InfoPublishAssistantTheme(isInDarkTheme = true) {
                val settings by appContainer.displaySettingsStore.settings.collectAsStateWithLifecycle()
                RotatedRoot(rotation = settings.rotation) { content() }
            }
        }
    }

    /**
     * 在事件分发给 Compose 之前完成方向键换算。
     *
     * 这里直接读 `settings.value`（StateFlow 可同步取值），而不是镜像成 Activity 字段，
     * 避免在组合过程中写外部状态。
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val settings = appContainer.displaySettingsStore.settings.value
        if (!settings.dpadFollowsRotation) return super.dispatchKeyEvent(event)

        val remappedCode = DpadRemapper.remapKeyCode(event.keyCode, settings.rotation)
        if (remappedCode == event.keyCode) return super.dispatchKeyEvent(event)

        return super.dispatchKeyEvent(event.withKeyCode(remappedCode))
    }
}
