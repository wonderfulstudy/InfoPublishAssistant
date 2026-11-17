package com.jyf.infopublishassistant.data.remote

import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame

/**
 * 远端开奖数据源契约。
 *
 * 【这是留给你实现的部分】整个 App 只依赖这个接口，
 * 你可以用 Retrofit / OkHttp / Ktor / HttpURLConnection 任意实现，
 * 再把 [com.jyf.infopublishassistant.core.platform.AppContainer] 里的
 * 绑定从 [SampleLotteryRemoteDataSource] 换成你的实现即可，UI 与缓存层无需改动。
 *
 * 实现约定：
 * - 结果按期号从新到旧返回；
 * - 网络异常直接抛出，不要吞掉返回空列表（空列表会被当作「没有更多数据」）；
 * - 需要把接口返回的 DTO 映射为 [DrawResult]，[DrawResult.drawResult] 的分组顺序
 *   必须与 [LotteryGame.zones] 一致。
 */
interface LotteryRemoteDataSource {

    /**
     * @param game 彩票编号
     * @param page 1 起始的页码，第 1 页为最新一期
     * @param pageSize 期望条数；返回条数少于该值即视为最后一页
     */
    suspend fun fetchDrawHistory(
        game: LotteryGame,
        page: Int,
        pageSize: Int = 100,
    ): List<DrawResult>
}
