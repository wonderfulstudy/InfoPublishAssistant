package com.jyf.infopublishassistant.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jyf.infopublishassistant.R
import com.jyf.infopublishassistant.core.display.DisplaySettings
import com.jyf.infopublishassistant.ui.component.TvFocusSurface
import com.jyf.infopublishassistant.ui.component.TvTopBar
import com.jyf.infopublishassistant.ui.theme.ScreenBackground
import com.jyf.infopublishassistant.ui.theme.SurfaceBackground
import com.jyf.infopublishassistant.ui.theme.TextPrimary
import com.jyf.infopublishassistant.ui.theme.TextSecondary

/**
 * 设置页。
 *
 * 注意旋转是「即时生效」的：点一下旋转按钮，本页立刻转过去，
 * 因为所有页面观察的是同一个 [com.jyf.infopublishassistant.core.display.DisplaySettingsStore]
 * 单例 StateFlow，返回上一页时它也已经转好了。
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: DisplaySettings,
    onCycleRotation: () -> Unit,
    onToggleDpadFollowsRotation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstItemFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 40.dp, vertical = 32.dp),
    ) {
        TvTopBar(title = stringResource(R.string.title_settings))

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingRow(
                title = stringResource(R.string.setting_screen_rotation),
                value = "${settings.rotation.degrees.toInt()}°",
                hint = stringResource(R.string.setting_screen_rotation_hint),
                onClick = onCycleRotation,
                modifier = Modifier.focusRequester(firstItemFocusRequester),
            )
            SettingRow(
                title = stringResource(R.string.setting_dpad_follow_rotation),
                value = stringResource(
                    if (settings.dpadFollowsRotation) R.string.setting_on else R.string.setting_off,
                ),
                hint = stringResource(R.string.setting_dpad_follow_rotation_hint),
                onClick = onToggleDpadFollowsRotation,
            )
        }
    }

    LaunchedEffect(Unit) {
        runCatching { firstItemFocusRequester.requestFocus() }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingRow(
    title: String,
    value: String,
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TvFocusSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = SurfaceBackground,
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 20.dp),
    ) { isFocused ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isFocused) TextPrimary else TextSecondary,
                    fontSize = 22.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = hint, color = TextSecondary, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            )
        }
    }
}
