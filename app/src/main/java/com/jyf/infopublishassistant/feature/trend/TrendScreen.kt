package com.jyf.infopublishassistant.feature.trend

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jyf.infopublishassistant.R
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.ui.theme.DrawNumberTextColor
import com.jyf.infopublishassistant.ui.theme.IssueTextColor
import com.jyf.infopublishassistant.ui.theme.ScreenBackground
import com.jyf.infopublishassistant.ui.theme.TextSecondary
import com.jyf.infopublishassistant.ui.theme.TrendZoneBackgrounds
import kotlinx.coroutines.launch

private val ISSUE_COLUMN_WIDTH = 45.dp
private val PLAIN_NUMBERS_COLUMN_WIDTH = 85.dp
private val ROW_HEIGHT = 15.dp
private val MIN_SLOT_WIDTH = 6.dp
private val TREND_LINE_WIDTH = 1.dp

/** 期号列字号；需小于 [ROW_HEIGHT] 否则文字会被行高裁切。 */
private val ISSUE_FONT_SIZE = 11.sp

/** 开奖号码纯文本列字号。 */
private val PLAIN_NUMBERS_FONT_SIZE = 10.sp

/** 分区内号码字号上限；实际字号按子列宽自动推导，见 TrendGroupCell。 */
private val MARK_FONT_SIZE = 10.sp

/** 距离列表底部还剩这么多项时预取下一页，避免用户滚到底才开始等网络。 */
private const val PREFETCH_DISTANCE = 5

/** 遥控器上/下键一次滚动的期数。 */
private const val SCROLL_PAGE_SIZE = 3

/**
 * 走势图页面：按期号倒序列出历史开奖，每行是一张走势图行——
 * 「期号 +（可选）开奖号码纯文本 + 若干分区」，分区列结构由 [LotteryGame.trendLayout] 数据驱动。
 *
 * 分区内按取值等分子列，开奖号码落在数值对应子列；带连接线的分区把相邻两期号码连起来，
 * 差值为 1 时不连（[shouldConnectTrendLine]）。行高固定且行间距为 0，
 * 每行只画本行内的上下两个半段线，行边界处取两期号码横坐标中点，上下行半段正好拼成完整直线。
 * 不同分区按顺序取 [TrendZoneBackgrounds] 的低饱和背景色。
 *
 * 遥控器上/下键在 [LazyColumn] 上被消费，一次滚动 [SCROLL_PAGE_SIZE] 期，焦点不逐行移动。
 * 焦点放在列表容器本身而不是某一行：`focusable()` 不绘制任何视觉，
 * 因此进入页面时不会出现「第一行被高亮」，同时列表内仍有聚焦节点来接收遥控器按键
 * （完全没有焦点的话，上/下键不会路由到列表，滚动就会失效）。
 * 显示顺序与缓存相反：缓存按新→旧存储（分页页码换算依赖该顺序），页面反转为旧→新、
 * 最新一期在底部，进入页面自动滚到最新一行；接近顶部时自动触发 [onLoadMore] 拉取更早的期数，
 * 已无更早数据时按向上键到顶弹出提示。
 *
 * 文字颜色通过 [issueColor] / [numberColor] 配置，默认取
 * `IssueTextColor` / `DrawNumberTextColor`，可在 ui/theme/Color.kt 里统一修改。
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TrendScreen(
    game: LotteryGame,
    uiState: TrendUiState,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    issueColor: Color = IssueTextColor,
    numberColor: Color = DrawNumberTextColor,
) {
    val listState = rememberLazyListState()
    val listFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val layout = remember(game) { game.trendLayout() }
    // 缓存按新→旧存储（分页页码换算依赖该顺序），走势图按阅读习惯反转：旧→新、最新在底部
    val draws = remember(uiState.draws) { uiState.draws.asReversed() }

    /**
     * 以当前可见区为锚点滚动 [delta] 期。
     * 向下用「最后一个可见项」做锚、向上用「第一个可见项」做锚，
     * 这样连续按同一方向时视口是连续推进的，不会来回跳。
     */
    fun jumpBy(delta: Int) {
        scope.launch {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            if (total == 0) return@launch

            val anchor = if (delta > 0) {
                info.visibleItemsInfo.lastOrNull()?.index ?: 0
            } else {
                info.visibleItemsInfo.firstOrNull()?.index ?: 0
            }
            val target = (anchor + delta).coerceIn(0, total - 1)
            listState.scrollToItem(target)

            // 已经停在最旧一期且确认没有更多数据时，才提示「到底了」；
            // 若 hasMore 仍为 true，说明只是还没加载回来，交给预取逻辑继续拉。
            if (delta < 0 && target == 0 && !uiState.hasMore) {
                Toast
                    .makeText(context, context.getString(R.string.trend_no_more), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // 不留任何内边距：走势图信息铺满整块屏幕，边距只会浪费可显示期数。
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.draws.isEmpty() && !uiState.isIdleLoading -> Text(
                    text = uiState.errorMessage
                        ?.let { stringResource(R.string.trend_load_failed, it) }
                        ?: stringResource(R.string.trend_empty),
                    color = TextSecondary,
                    fontSize = 18.sp,
                )

                uiState.draws.isEmpty() -> Text(
                    text = stringResource(R.string.trend_loading),
                    color = TextSecondary,
                    fontSize = 18.sp,
                )

                else -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    // 子列宽度按剩余屏幕宽度均分：彩种分区越多每列越窄，保证整张表不横向溢出。
                    val fixedWidth =
                        ISSUE_COLUMN_WIDTH +
                            if (layout.showPlainNumbers) PLAIN_NUMBERS_COLUMN_WIDTH else 0.dp
                    val slotWidth = maxOf(
                        MIN_SLOT_WIDTH,
                        (maxWidth - fixedWidth) / layout.totalSlots,
                    )

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            // 焦点给列表容器：不画视觉、不落在某一行上，但保证能接收按键
                            .focusRequester(listFocusRequester)
                            .focusable()
                            // onPreviewKeyEvent 在子节点之前触发，返回 true 即消费该按键，
                            // 行内的逐行焦点移动就不会再发生，从而实现「按一次滑 3 期」。
                            .onPreviewKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown) {
                                    return@onPreviewKeyEvent false
                                }
                                when (event.key) {
                                    Key.DirectionDown -> {
                                        jumpBy(SCROLL_PAGE_SIZE)
                                        true
                                    }

                                    Key.DirectionUp -> {
                                        jumpBy(-SCROLL_PAGE_SIZE)
                                        true
                                    }

                                    else -> false
                                }
                            },
                    ) {
                        itemsIndexed(
                            items = draws,
                            // 期号在同一彩种内唯一，作为 key 可让分页插入时复用已有行
                            key = { _, draw -> draw.drawNum },
                        ) { index, draw ->
                            TrendChartRow(
                                layout = layout,
                                draw = draw,
                                previous = draws.getOrNull(index - 1),
                                next = draws.getOrNull(index + 1),
                                slotWidth = slotWidth,
                                issueColor = issueColor,
                                numberColor = numberColor,
                            )
                        }
                    }
                }
            }
        }
    }

    // 接近顶部（最旧一端）时预取更早的期数
    val firstVisibleIndex by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1 }
    }
    LaunchedEffect(firstVisibleIndex, uiState.draws.size) {
        if (firstVisibleIndex in 0..PREFETCH_DISTANCE) {
            onLoadMore()
        }
    }

    // 数据首次到达后滚到最新一期（列表底部）并把焦点放到列表容器（不是某一行），
    // 列表才能接收遥控器按键。
    // 兜住异常的原因同 GameSelectScreen：FocusRequester 未附着时会抛异常，
    // 而抢焦点失败不应该让页面崩溃。
    LaunchedEffect(uiState.draws.isNotEmpty()) {
        if (uiState.draws.isNotEmpty()) {
            runCatching {
                listState.scrollToItem(draws.size - 1)
                listFocusRequester.requestFocus()
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TrendChartRow(
    layout: TrendLayout,
    draw: DrawResult,
    previous: DrawResult?,
    next: DrawResult?,
    slotWidth: Dp,
    issueColor: Color,
    numberColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().height(ROW_HEIGHT)) {
        Box(
            modifier = Modifier
                .width(ISSUE_COLUMN_WIDTH)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(R.string.trend_issue_format, draw.drawNum),
                color = issueColor,
                fontSize = ISSUE_FONT_SIZE,
                maxLines = 1,
            )
        }
        if (layout.showPlainNumbers) {
            Box(
                modifier = Modifier
                    .width(PLAIN_NUMBERS_COLUMN_WIDTH)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = draw.drawResult.flatten().joinToString(" "),
                    color = numberColor,
                    fontSize = PLAIN_NUMBERS_FONT_SIZE,
                    maxLines = 1,
                )
            }
        }
        layout.groups.forEachIndexed { groupIndex, group ->
            TrendGroupCell(
                group = group,
                groupIndex = groupIndex,
                draw = draw,
                previous = previous,
                next = next,
                slotWidth = slotWidth,
                defaultColor = numberColor,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TrendGroupCell(
    group: TrendGroup,
    groupIndex: Int,
    draw: DrawResult,
    previous: DrawResult?,
    next: DrawResult?,
    slotWidth: Dp,
    defaultColor: Color,
    modifier: Modifier = Modifier,
) {
    val color = group.textColor ?: defaultColor
    val marks = remember(group, draw) { group.values(draw).distinct() }
    val current = if (group.connectLines) group.values(draw).firstOrNull() else null
    val above = if (group.connectLines) previous?.let { group.values(it).firstOrNull() } else null
    val below = if (group.connectLines) next?.let { group.values(it).firstOrNull() } else null
    // 两位数字宽度约 1.2 倍字号；按子列宽反推字号并以 MARK_FONT_SIZE 封顶，
    // 窄屏（旋转后）不会再把个位换行裁掉
    val markFontSize = minOf(MARK_FONT_SIZE.value, slotWidth.value * 0.8f).sp

    Box(
        modifier = modifier
            .width(slotWidth * group.range.count())
            .fillMaxHeight()
            .background(TrendZoneBackgrounds[groupIndex % TrendZoneBackgrounds.size]),
    ) {
        if (current != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val slot = slotWidth.toPx()
                fun xOf(value: Int) = (value - group.range.first + 0.5f) * slot
                val middle = size.height / 2f
                // 每行只画本行内的两个半段，行边界端点取两期号码横坐标中点，
                // 与相邻行画的半段首尾相接，拼成一条完整直线且不会越界绘制。
                if (above != null && shouldConnectTrendLine(current, above)) {
                    drawLine(
                        color = color,
                        start = Offset((xOf(above) + xOf(current)) / 2f, 0f),
                        end = Offset(xOf(current), middle),
                        strokeWidth = TREND_LINE_WIDTH.toPx(),
                    )
                }
                if (below != null && shouldConnectTrendLine(current, below)) {
                    drawLine(
                        color = color,
                        start = Offset(xOf(current), middle),
                        end = Offset((xOf(current) + xOf(below)) / 2f, size.height),
                        strokeWidth = TREND_LINE_WIDTH.toPx(),
                    )
                }
            }
        }
        Row {
            for (value in group.range) {
                Box(
                    modifier = Modifier
                        .width(slotWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (value in marks) {
                        Text(
                            text = value.toString(),
                            color = color,
                            fontSize = markFontSize,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}
