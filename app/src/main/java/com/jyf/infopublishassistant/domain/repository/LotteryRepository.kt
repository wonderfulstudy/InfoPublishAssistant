package com.jyf.infopublishassistant.domain.repository

import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import kotlinx.coroutines.flow.Flow

/**
 * 开奖数据仓库：本地缓存（Room）为唯一数据源，网络只负责写入缓存。
 *
 * 这样 UI 永远只观察一个 [Flow]，离线时展示缓存、联网时自动刷新，
 * 无需在界面层处理两套数据流。
 */
interface LotteryRepository {

    /**
     * 观察指定彩种已缓存的全部开奖记录，按期号从新到旧排列。
     * 缓存内容变化时会自动重新发射。
     */
    fun observeDrawHistory(game: LotteryGame): Flow<List<DrawResult>>

    /**
     * 拉取一页远端数据并写入缓存。
     *
     * @param page 1 起始的页码，第 1 页为最新数据
     * @return 本页取到的记录数；返回 0 表示已无更多数据，
     *   返回值小于 [pageSize] 表示这是最后一页。
     * @throws Exception 网络失败时抛出，由调用方决定如何呈现
     */
    suspend fun loadPage(game: LotteryGame, page: Int, pageSize: Int): Int

    /**
     * 当前已缓存的记录数。
     *
     * 分页时用它换算下一页页码，而不是依赖 Flow 里已经发射的列表长度：
     * Room 的 Flow 是异步发射的，刚 upsert 完就读列表长度会拿到旧值，导致重复请求同一页。
     */
    suspend fun cachedCount(game: LotteryGame): Int
}
