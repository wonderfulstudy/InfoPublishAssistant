package com.jyf.infopublishassistant.feature.trend

import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.domain.repository.LotteryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class TrendViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    // viewModelScope 跑在 Main 上，必须让 Main 与 runTest 共用同一个调度器，
    // 否则 advanceUntilIdle 推不动 ViewModel 里的协程
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshes page one on start`() = runTest(testDispatcher) {
        val repository = FakeLotteryRepository(totalDraws = 50)
        val viewModel = createViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(listOf(1), repository.requestedPages)
        assertEquals(TrendViewModel.PAGE_SIZE, viewModel.uiState.value.draws.size)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `walks pages in order and stops after a partial page`() = runTest(testDispatcher) {
        // 50 期 = 20 + 20 + 10，第三页不满即视为到底
        val repository = FakeLotteryRepository(totalDraws = 50)
        val viewModel = createViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(listOf(1, 2), repository.requestedPages)
        assertEquals(40, viewModel.uiState.value.draws.size)
        assertTrue(viewModel.uiState.value.hasMore)

        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(listOf(1, 2, 3), repository.requestedPages)
        assertEquals(50, viewModel.uiState.value.draws.size)
        assertFalse("返回不足一页后不应再认为有更多数据", viewModel.uiState.value.hasMore)

        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals("hasMore=false 时应拦下请求", listOf(1, 2, 3), repository.requestedPages)
    }

    @Test
    fun `derives the next page from the cached row count`() = runTest(testDispatcher) {
        // 这是分页里最容易出错的一处：页码必须由「已缓存条数」实时换算。
        // 若改成内存里自增，重新进入页面时会把已缓存的第 2 页重新拉一遍，
        // 表现为「滚到底一直转圈但列表不增长」。
        val repository = FakeLotteryRepository(totalDraws = 100, preCachedCount = 40)
        val viewModel = createViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals("进页面仍应刷新第 1 页以拿到最新一期", listOf(1), repository.requestedPages)

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals("缓存 40 条时下一页应是第 3 页", listOf(1, 3), repository.requestedPages)
        assertEquals(60, viewModel.uiState.value.draws.size)
    }

    @Test
    fun `keeps cached draws and surfaces the error when loading fails`() = runTest(testDispatcher) {
        val repository = FakeLotteryRepository(totalDraws = 50)
        val viewModel = createViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val drawsBeforeFailure = viewModel.uiState.value.draws
        repository.shouldFail = true

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals("网络失败不能清空已展示的缓存数据", drawsBeforeFailure, viewModel.uiState.value.draws)
        assertEquals("network down", viewModel.uiState.value.errorMessage)
        assertFalse("失败后必须退出加载中状态，否则页面会一直转圈", viewModel.uiState.value.isLoadingMore)
    }

    @Test
    fun `recovers after a failed load`() = runTest(testDispatcher) {
        val repository = FakeLotteryRepository(totalDraws = 50)
        val viewModel = createViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        repository.shouldFail = true
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasMore)

        repository.shouldFail = false
        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.errorMessage)
        assertEquals(listOf(1, 2, 2), repository.requestedPages)
        assertEquals(40, viewModel.uiState.value.draws.size)
    }

    @Test
    fun `ignores loadNextPage while a request is already in flight`() = runTest(testDispatcher) {
        val repository = FakeLotteryRepository(totalDraws = 100)
        val viewModel = createViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        viewModel.loadNextPage()
        viewModel.loadNextPage()
        viewModel.loadNextPage()

        assertEquals("滚动过程中重复触发只应发出一次请求", listOf(1, 2), repository.requestedPages)

        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf(1, 2), repository.requestedPages)
        assertEquals(40, viewModel.uiState.value.draws.size)
    }

    private fun createViewModel(repository: LotteryRepository) =
        TrendViewModel(repository, LotteryGame.DA_LE_TOU)

    /**
     * 用内存列表模拟「Room 缓存 + 远端分页」。
     * [drawsFlow] 扮演 Room 的 Flow：写入后自动发射，UI 只观察这一个流。
     */
    private class FakeLotteryRepository(
        totalDraws: Int,
        preCachedCount: Int = 0,
    ) : LotteryRepository {

        val requestedPages = mutableListOf<Int>()
        var shouldFail = false
        var gate: CompletableDeferred<Unit>? = null

        private val allDraws = (0 until totalDraws).map { index ->
            DrawResult(
                // 期号随 index 递减，模拟真实的时间倒序
                issue = (1_000_000 - index).toString(),
                numbers = listOf(listOf(index % 35 + 1, index % 12 + 1)),
            )
        }

        private val cached = allDraws.take(preCachedCount).toMutableList()
        private val drawsFlow = MutableStateFlow<List<DrawResult>>(cached.toList())

        override fun observeDrawHistory(game: LotteryGame): Flow<List<DrawResult>> = drawsFlow

        override suspend fun loadPage(game: LotteryGame, page: Int, pageSize: Int): Int {
            requestedPages += page
            gate?.await()
            if (shouldFail) throw IOException("network down")

            val start = (page - 1) * pageSize
            if (start >= allDraws.size) return 0
            val slice = allDraws.subList(start, minOf(start + pageSize, allDraws.size))

            // 按主键去重，模拟 Room 的 @Upsert
            val knownIssues = cached.map { it.issue }.toSet()
            cached.addAll(slice.filterNot { it.issue in knownIssues })
            drawsFlow.value = cached.toList()
            return slice.size
        }

        override suspend fun cachedCount(game: LotteryGame): Int = cached.size
    }
}
