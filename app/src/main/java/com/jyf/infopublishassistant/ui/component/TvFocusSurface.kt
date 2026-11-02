package com.jyf.infopublishassistant.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.jyf.infopublishassistant.ui.theme.FocusBackground
import com.jyf.infopublishassistant.ui.theme.FocusBorder
import com.jyf.infopublishassistant.ui.theme.IdleBorder

/**
 * 遥控器可聚焦的容器，统一焦点视觉与「聚焦时滚动到可见区域」。
 *
 * 显式调用 [BringIntoViewRequester] 而不依赖 `focusable()` 的隐式行为：
 * 长列表里如果聚焦项没有被自动滚入视口，焦点会移到屏幕外的行而画面不动，
 * 遥控器看起来就像「卡住」了 —— 这是 TV 应用最难排查的一类问题。
 *
 * @param onClick 传 null 表示该项只需要能被聚焦、不需要点击（例如走势图的期数行）。
 * @param focusedScale 聚焦时的放大倍数。密集列表请保持 1f：放大后相邻行会互相遮挡；
 *   卡片式布局可以设成 1.04f 左右。
 */
@Composable
fun TvFocusSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(10.dp),
    backgroundColor: Color = Color.Transparent,
    focusedScale: Float = 1f,
    focusHighlight: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable (isFocused: Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isFocused) {
        if (isFocused) bringIntoViewRequester.bringIntoView()
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "focusScale",
    )

    val focusModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier.focusable(interactionSource = interactionSource)
    }

    Box(
        // 焦点与内边距始终保留：focusHighlight 只控制「画不画」焦点背景/边框/放大。
        // 不能把 focusable 一起关掉，否则列表收不到遥控器按键；
        // 也不能丢掉 contentPadding，否则内容会贴到容器边缘。
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .then(
                if (focusHighlight) {
                    Modifier
                        .scale(scale)
                        .clip(shape)
                        .background(if (isFocused) FocusBackground else backgroundColor)
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = if (isFocused) FocusBorder else IdleBorder,
                            shape = shape,
                        )
                } else {
                    Modifier
                }
            )
            .then(focusModifier)
            .padding(contentPadding),
    ) {
        content(isFocused)
    }
}
