package com.jyf.infopublishassistant.feature.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jyf.infopublishassistant.core.platform.BaseTvActivity

class SettingsActivity : BaseTvActivity() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.factory(appContainer.displaySettingsStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRotatedContent {
            val settings = viewModel.settings.collectAsStateWithLifecycle().value
            SettingsScreen(
                settings = settings,
                onCycleRotation = viewModel::cycleRotation,
                onToggleDpadFollowsRotation = viewModel::toggleDpadFollowsRotation,
            )
        }
    }
}
