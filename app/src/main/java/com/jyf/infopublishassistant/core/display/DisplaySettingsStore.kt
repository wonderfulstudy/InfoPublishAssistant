package com.jyf.infopublishassistant.core.display

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 显示相关的全局设置。 */
data class DisplaySettings(
    val rotation: ScreenRotation = ScreenRotation.DEFAULT,
    /**
     * 是否按画面旋转角度重映射遥控器方向键。
     * 详见 [DpadRemapper] 中关于 Compose 焦点搜索是否已计入旋转变换的说明。
     */
    val dpadFollowsRotation: Boolean = true,
)

/**
 * 显示设置的读写入口。
 *
 * 以 DataStore 为持久化后端，但对外暴露进程内单例 [StateFlow]：
 * 这样在设置页改变旋转角度时，所有已打开的 Activity 会立即跟着旋转，
 * 无需返回重进；[BaseTvActivity] 重映射按键时也能同步读到 `settings.value`。
 *
 * 首帧可能短暂使用 [DisplaySettings] 默认值（DataStore 首次读盘是异步的），
 * 通过 [SharingStarted.Eagerly] 在 Application 启动时就开始加载以缩短该窗口。
 */
class DisplaySettingsStore(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope,
) {

    val settings: StateFlow<DisplaySettings> = dataStore.data
        .map { preferences ->
            DisplaySettings(
                rotation = ScreenRotation.fromName(preferences[KEY_ROTATION]),
                dpadFollowsRotation = preferences[KEY_DPAD_FOLLOWS_ROTATION] ?: true,
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, DisplaySettings())

    suspend fun setRotation(rotation: ScreenRotation) {
        dataStore.edit { it[KEY_ROTATION] = rotation.name }
    }

    suspend fun cycleRotation() {
        setRotation(settings.value.rotation.next())
    }

    suspend fun setDpadFollowsRotation(enabled: Boolean) {
        dataStore.edit { it[KEY_DPAD_FOLLOWS_ROTATION] = enabled }
    }

    suspend fun toggleDpadFollowsRotation() {
        setDpadFollowsRotation(!settings.value.dpadFollowsRotation)
    }

    private companion object {
        val KEY_ROTATION: Preferences.Key<String> = stringPreferencesKey("screen_rotation")
        val KEY_DPAD_FOLLOWS_ROTATION: Preferences.Key<Boolean> =
            booleanPreferencesKey("dpad_follows_rotation")
    }
}
