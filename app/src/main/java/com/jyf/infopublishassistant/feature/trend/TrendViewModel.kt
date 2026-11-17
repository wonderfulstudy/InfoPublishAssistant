package com.jyf.infopublishassistant.feature.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 走势图页面状态。
 *
 * [draws] 来自 Room 的 Flow，因此缓存命中时会先立即显示历史数据，
 * 网络刷新完成后再自动更新，用户不会看到白屏。
 */
data class TrendUiState(
    val draws: List<DrawResult> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
) {
    val isIdleLoading: Boolean get() = draws.isEmpty() && (isRefreshing || isLoadingMore)
}

/**
 * 走势图 ViewModel，由 [LotteryGame] 参数化，五个彩种共用同一套逻辑。
 */
class TrendViewModel(
    private val repository: LotteryRepository,
    private val game: LotteryGame,
    private val cacheTarget: Int = HISTORY_CACHE_TARGET,
) : ViewModel() {

    private val pagingState = MutableStateFlow(PagingState())

    val uiState: StateFlow<TrendUiState> =
        combine(repository.observeDrawHistory(game), pagingState) { draws, paging ->
            TrendUiState(
                draws = draws,
                isRefreshing = paging.isRefreshing,
                isLoadingMore = paging.isLoadingMore,
                hasMore = paging.hasMore,
                errorMessage = paging.errorMessage,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            // 初始就标记为刷新中，否则首帧会闪一下「暂无开奖数据」
            initialValue = TrendUiState(isRefreshing = true),
        )

    init {
        // 进页面先拉第 1 页取最新一期。upsert 以 (game, drawNum) 为主键去重，
        // 所以已有缓存时重复拉第 1 页不会产生重复行。
        viewModelScope.launch {
            loadPage(page = 1, isRefresh = true)
            fillCacheToTarget()
        }
    }

    /** 首屏刷新后把缓存补到 [cacheTarget] 期；已达标时不发任何请求。 */
    private suspend fun fillCacheToTarget() {
        if (repository.cachedCount(game) >= cacheTarget) return
        pagingState.update { it.copy(isRefreshing = true, errorMessage = null) }
        runCatching { repository.fillCacheTo(game, cacheTarget, PAGE_SIZE) }.fold(
            onSuccess = { hasMore ->
                pagingState.update { it.copy(isRefreshing = false, hasMore = hasMore) }
            },
            onFailure = { error ->
                pagingState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = error.message ?: error.javaClass.simpleName,
                    )
                }
            },
        )
    }

    /** 列表滚动接近底部时由 UI 调用。 */
    fun loadNextPage() {
        val current = pagingState.value
        if (current.isRefreshing || current.isLoadingMore || !current.hasMore) return

        viewModelScope.launch {
            // 页码用「缓存条数 ÷ 页大小」实时换算，而不是记在内存里自增。
            // 好处是自我校正：中途有新开奖插入导致页边界漂移时，下一次仍会落在正确页，
            // 也不会出现「重复进入页面后把已缓存的页从头再走一遍」的问题。
            val page = repository.cachedCount(game) / PAGE_SIZE + 1
            loadPage(page = page, isRefresh = false)
        }
    }

    private suspend fun loadPage(page: Int, isRefresh: Boolean) {
        pagingState.update {
            it.copy(
                isRefreshing = isRefresh,
                isLoadingMore = !isRefresh,
                errorMessage = null,
            )
        }

        runCatching { repository.loadPage(game, page, PAGE_SIZE) }.fold(
            onSuccess = { loadedCount ->
                pagingState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        // 返回不足一页即说明到底了
                        hasMore = loadedCount == PAGE_SIZE,
                    )
                }
            },
            onFailure = { error ->
                pagingState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = error.message ?: error.javaClass.simpleName,
                    )
                }
            },
        )
    }

    private data class PagingState(
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val errorMessage: String? = null,
    )

    companion object {
        /** 与官方接口验证过的 pageSize 一致；改动它同时影响补缓存的请求次数。 */
        const val PAGE_SIZE = 30

        /** 本地缓存保留「当前往前」的期数，进页面后在后台补齐。 */
        const val HISTORY_CACHE_TARGET = 500
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(repository: LotteryRepository, game: LotteryGame): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { TrendViewModel(repository, game) }
            }
    }
}
