package com.jyf.infopublishassistant

import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.tooling.preview.Preview
import com.jyf.infopublishassistant.core.platform.BaseTvActivity
import com.jyf.infopublishassistant.feature.gameselect.GameSelectScreen
import com.jyf.infopublishassistant.feature.settings.SettingsActivity
import com.jyf.infopublishassistant.feature.trend.trendActivityClass

/**
 * 应用入口：游戏选择页。
 *
 * 直接用 MainActivity 承载选择页，不再额外套一层 Activity —— 启动即所见，
 * 少一次跳转也少一次焦点丢失的机会。
 */
class MainActivity : BaseTvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRotatedContent {
            GameSelectScreen(
                onGameSelected = { game ->
                    startActivity(Intent(this, game.trendActivityClass()))
                },
                onOpenSettings = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                },
            )
        }
    }
}
