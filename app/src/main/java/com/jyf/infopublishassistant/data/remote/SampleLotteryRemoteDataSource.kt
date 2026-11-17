package com.jyf.infopublishassistant.data.remote

import android.content.Context
import com.jyf.infopublishassistant.R
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * 中国体育彩票历史开奖接口实现：
 * `GET {base_url}{get_history_path}?gameNo={彩种编号}&provinceId=0&pageSize={n}&isVerify=1&pageNo={page}`
 *
 * 每期只取两件事：`value.list[].lotteryDrawNum`（期号）与 `lotteryDrawResult`
 * （空格分隔的扁平开奖号码串）。号码串按 [LotteryGame.zones] 的 count 顺序切组后
 * 写入 [DrawResult.drawResult]，因此接口字段变化只需要改 [parseHistory]。
 *
 * 请求在 [Dispatchers.IO] 上执行；非 200 响应直接抛 [IOException]，
 * 不吞异常返回空列表（空列表会被上层当作「没有更多数据」）。
 */
class SampleLotteryRemoteDataSource(private val context: Context) : LotteryRemoteDataSource {

    override suspend fun fetchDrawHistory(
        game: LotteryGame,
        page: Int,
        pageSize: Int,
    ): List<DrawResult> = withContext(Dispatchers.IO) {
        val url = URL(
            buildHistoryUrl(
                baseUrl = context.getString(R.string.base_url),
                historyPath = context.getString(R.string.get_history_path),
                game = game,
                page = page,
                pageSize = pageSize,
            ),
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("HTTP $code")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseHistory(body, game)
        } finally {
            connection.disconnect()
        }
    }

    companion object {

        internal fun buildHistoryUrl(
            baseUrl: String,
            historyPath: String,
            game: LotteryGame,
            page: Int,
            pageSize: Int,
        ): String {
            require(page >= 1) { "pageNo 从 1 开始，实际 $page" }
            require(pageSize > 0) { "pageSize 必须为正，实际 $pageSize" }
            // base_url 与 path 在 strings.xml 里可能带或不带斜杠，这里保证恰好一个分隔斜杠
            val base = baseUrl.trimEnd('/')
            val path = if (historyPath.startsWith("/")) historyPath else "/$historyPath"
            return "$base$path" +
                "?gameNo=${game.apiGameNo}&provinceId=0&pageSize=$pageSize&isVerify=1&pageNo=$page"
        }

        internal fun parseHistory(body: String, game: LotteryGame): List<DrawResult> {
            val root = JSONObject(body)
            // 网关对错误路径/参数仍返回 HTTP 200 + 不含 value 的错误体，
            // 这里转成 IOException 并带上接口 errorMessage，避免页面只显示难懂的解析异常
            val value = root.optJSONObject("value")
                ?: throw IOException("接口未返回 value：${root.optString("errorMessage", "unknown error")}")
            val list = value.getJSONArray("list")
            return (0 until list.length()).map { index ->
                val item = list.getJSONObject(index)
                DrawResult(
                    drawNum = item.getString("lotteryDrawNum"),
                    drawResult = splitByZones(item.getString("lotteryDrawResult"), game),
                )
            }
        }

        /** 号码串缺号时对应区返回空列表，界面层按空渲染而不是崩溃。 */
        private fun splitByZones(rawResult: String, game: LotteryGame): List<List<Int>> {
            val numbers = rawResult.trim().split(' ').filter { it.isNotEmpty() }.map { it.toInt() }
            var offset = 0
            return game.zones.map { zone ->
                val end = minOf(offset + zone.count, numbers.size)
                val zoneNumbers = if (offset >= numbers.size) emptyList() else numbers.subList(offset, end)
                offset += zone.count
                zoneNumbers.toList()
            }
        }
    }
}
