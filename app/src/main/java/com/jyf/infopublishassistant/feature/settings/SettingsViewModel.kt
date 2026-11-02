package com.jyf.infopublishassistant.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jyf.infopublishassistant.core.display.DisplaySettings
import com.jyf.infopublishassistant.core.display.DisplaySettingsStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val displaySettingsStore: DisplaySettingsStore,
) : ViewModel() {

    val settings: StateFlow<DisplaySettings> = displaySettingsStore.settings

    /** 按 0° → 90° → 180° → 270° 循环切换。所有已打开页面会同时跟着旋转。 */
    fun cycleRotation() {
        viewModelScope.launch { displaySettingsStore.cycleRotation() }
    }

    fun toggleDpadFollowsRotation() {
        viewModelScope.launch { displaySettingsStore.toggleDpadFollowsRotation() }
    }

    companion object {
        fun factory(store: DisplaySettingsStore): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(store) }
        }
    }
}
