package com.jyf.infopublishassistant

import android.app.Application
import com.jyf.infopublishassistant.core.platform.AppContainer

class InfoPublishApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // 访问 settings 会触发 stateIn(Eagerly) 立即开始读盘，
        // 缩短首帧仍在使用默认旋转角度的时间窗口
        container.displaySettingsStore.settings
    }
}
