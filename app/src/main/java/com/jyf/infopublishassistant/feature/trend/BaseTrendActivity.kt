package com.jyf.infopublishassistant.feature.trend

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jyf.infopublishassistant.core.platform.BaseTvActivity
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.feature.settings.SettingsActivity

/**
 * 走势图页面基类。
 *
 * 五个彩种各有一个独立 Activity（满足「每个游戏独立页面」的要求，
 * 也便于单独配置 label / orientation / 主题），但业务逻辑全部收敛在这里：
 * 子类只需声明自己对应哪个 [LotteryGame]。
 *
 * 旋转与方向键重映射由 [BaseTvActivity] 统一处理，子类无需关心。
 */
abstract class BaseTrendActivity : BaseTvActivity() {

    protected abstract val game: LotteryGame

    private val viewModel: TrendViewModel by viewModels {
        TrendViewModel.factory(appContainer.lotteryRepository, game)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRotatedContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            TrendScreen(
                game = game,
                uiState = uiState,
                onLoadMore = viewModel::loadNextPage,
            )
        }
    }
}
