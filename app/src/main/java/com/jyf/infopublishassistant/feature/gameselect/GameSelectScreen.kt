package com.jyf.infopublishassistant.feature.gameselect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jyf.infopublishassistant.R
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.ui.component.TvFocusSurface
import com.jyf.infopublishassistant.ui.component.TvTextButton
import com.jyf.infopublishassistant.ui.component.TvTopBar
import com.jyf.infopublishassistant.ui.theme.ScreenBackground
import com.jyf.infopublishassistant.ui.theme.SurfaceBackground
import com.jyf.infopublishassistant.ui.theme.TextPrimary
import com.jyf.infopublishassistant.ui.theme.TextSecondary

/**
 * 游戏选择页：应用主入口，列出全部彩种，选中后进入对应走势图 Activity。
 *
 * 用自适应网格而不是固定横排：画面旋转到 90°/270° 后内容区变成竖长条，
 * 固定横排会挤成一团，`GridCells.Adaptive` 会自动改成 1~2 列。
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GameSelectScreen(
    onGameSelected: (LotteryGame) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val games = remember { LotteryGame.entries.toList() }
    val firstCardFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 40.dp, vertical = 32.dp),
    ) {
        TvTopBar(
            title = stringResource(R.string.title_game_select),
            subtitle = stringResource(R.string.game_select_hint),
            trailing = {
                TvTextButton(
                    text = stringResource(R.string.action_settings),
                    onClick = onOpenSettings,
                )
            },
        )

        Spacer(modifier = Modifier.height(28.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(games) { game ->
                val cardModifier = if (game == games.first()) {
                    Modifier.focusRequester(firstCardFocusRequester)
                } else {
                    Modifier
                }
                GameCard(
                    game = game,
                    onClick = { onGameSelected(game) },
                    modifier = cardModifier,
                )
            }
        }
    }

    // 首屏第一张卡片一定已经组合完成，因此这里可以安全地请求焦点。
    // 兜住异常是因为 FocusRequester 在未附着时会抛 IllegalStateException，
    // 而焦点没抢到不应该让整个页面崩溃——遥控器按任意方向键仍会由系统分配焦点。
    LaunchedEffect(Unit) {
        runCatching { firstCardFocusRequester.requestFocus() }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GameCard(
    game: LotteryGame,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TvFocusSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = SurfaceBackground,
        focusedScale = 1.04f,
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 32.dp),
    ) { isFocused ->
        Column {
            Text(
                text = game.displayName,
                color = TextPrimary,
                fontSize = 34.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = game.ruleSummary,
                color = TextSecondary,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "${game.totalBallCount} 个号码",
                color = if (isFocused) TextPrimary else TextSecondary,
                fontSize = 15.sp,
            )
        }
    }
}
