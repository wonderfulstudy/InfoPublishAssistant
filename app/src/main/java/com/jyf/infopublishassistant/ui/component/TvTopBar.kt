package com.jyf.infopublishassistant.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jyf.infopublishassistant.ui.theme.SurfaceBackground
import com.jyf.infopublishassistant.ui.theme.TextPrimary
import com.jyf.infopublishassistant.ui.theme.TextSecondary

/**
 * 页面顶部标题栏。[trailing] 用于放「设置」等按钮。
 *
 * 各页面共用，保证标题位置、字号在不同旋转角度下都一致。
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        trailing?.invoke()
    }
}

/**
 * 文字按钮。电视上没有触摸，用文字而不是图标，遥控器操作时意图更明确。
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TvFocusSurface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        backgroundColor = SurfaceBackground,
        focusedScale = 1.06f,
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
    ) { isFocused ->
        Text(
            text = text,
            color = if (isFocused) TextPrimary else TextSecondary,
            fontSize = 16.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/** 列表底部的状态提示（加载中 / 已全部加载 / 错误）。 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StatusFooter(
    text: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            color = if (isError) TextPrimary else TextSecondary,
            fontSize = 15.sp,
        )
    }
}
