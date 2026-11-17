package com.jyf.infopublishassistant.data.remote

import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

/**
 * 钉住官方历史开奖接口的请求与解析契约：
 * URL 上的 gameNo / 分页参数，以及 `lotteryDrawNum`、`lotteryDrawResult`
 * 到 [com.jyf.infopublishassistant.domain.model.DrawResult] 的映射。
 * 解析逻辑纯函数化放在 companion 里，因此无需网络即可测试。
 */
class SampleLotteryRemoteDataSourceTest {

    private val baseUrl = "https://webapi.sporttery.cn/gateway/lottery"
    private val historyPath = "/getHistoryPageListV1.qry"

    @Test
    fun `builds the history url with the game number and paging params`() {
        assertEquals(
            "$baseUrl$historyPath?gameNo=85&provinceId=0&pageSize=30&isVerify=1&pageNo=2",
            SampleLotteryRemoteDataSource.buildHistoryUrl(baseUrl, historyPath, LotteryGame.DLT, page = 2, pageSize = 30),
        )
        assertEquals(
            "$baseUrl$historyPath?gameNo=04&provinceId=0&pageSize=30&isVerify=1&pageNo=1",
            SampleLotteryRemoteDataSource.buildHistoryUrl(baseUrl, historyPath, LotteryGame.QXC, page = 1, pageSize = 30),
        )
        assertEquals(
            "$baseUrl$historyPath?gameNo=350133&provinceId=0&pageSize=30&isVerify=1&pageNo=1",
            SampleLotteryRemoteDataSource.buildHistoryUrl(baseUrl, historyPath, LotteryGame.PLYX, page = 1, pageSize = 30),
        )
    }

    @Test
    fun `joins base url and path with exactly one slash`() {
        assertEquals(
            "$baseUrl$historyPath?gameNo=85&provinceId=0&pageSize=30&isVerify=1&pageNo=1",
            SampleLotteryRemoteDataSource.buildHistoryUrl(baseUrl, "getHistoryPageListV1.qry", LotteryGame.DLT, page = 1, pageSize = 30),
        )
        assertEquals(
            SampleLotteryRemoteDataSource.buildHistoryUrl(baseUrl, historyPath, LotteryGame.DLT, 1, 30),
            SampleLotteryRemoteDataSource.buildHistoryUrl("$baseUrl/", historyPath, LotteryGame.DLT, 1, 30),
        )
    }

    @Test
    fun `an error envelope without value throws IOException`() {
        assertThrows(IOException::class.java) {
            SampleLotteryRemoteDataSource.parseHistory(
                """{"errorCode":"404","errorMessage":"接口不存在"}""",
                LotteryGame.DLT,
            )
        }
    }

    @Test
    fun `rejects a non positive page or page size`() {
        assertThrows(IllegalArgumentException::class.java) {
            SampleLotteryRemoteDataSource.buildHistoryUrl(baseUrl, historyPath, LotteryGame.DLT, page = 0, pageSize = 30)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SampleLotteryRemoteDataSource.buildHistoryUrl(baseUrl, historyPath, LotteryGame.DLT, page = 1, pageSize = 0)
        }
    }

    @Test
    fun `maps drawNum and splits the flat result by zone counts`() {
        val body = """
            {"value":{"list":[
              {"lotteryDrawNum":"26125","lotteryDrawResult":"12 13 23 25 29 07 10"},
              {"lotteryDrawNum":"26124","lotteryDrawResult":"01 05 18 22 33 04 09"}
            ]}}
        """.trimIndent()

        val draws = SampleLotteryRemoteDataSource.parseHistory(body, LotteryGame.DLT)

        assertEquals(listOf("26125", "26124"), draws.map { it.drawNum })
        assertEquals(listOf(listOf(12, 13, 23, 25, 29), listOf(7, 10)), draws[0].drawResult)
        assertEquals(listOf(listOf(1, 5, 18, 22, 33), listOf(4, 9)), draws[1].drawResult)
    }

    @Test
    fun `qi xing cai result splits into three positional zones`() {
        val body = """{"value":{"list":[{"lotteryDrawNum":"26125","lotteryDrawResult":"1 2 3 4 5 6 14"}]}}"""

        val draws = SampleLotteryRemoteDataSource.parseHistory(body, LotteryGame.QXC)

        assertEquals(listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(14)), draws[0].drawResult)
    }

    @Test
    fun `pai lie result stays a single ordered zone`() {
        val body = """{"value":{"list":[{"lotteryDrawNum":"26125","lotteryDrawResult":"4 7 3 8 6"}]}}"""

        val draws = SampleLotteryRemoteDataSource.parseHistory(body, LotteryGame.PLYX)

        assertEquals(listOf(listOf(4, 7, 3, 8, 6)), draws[0].drawResult)
    }

    @Test
    fun `an empty list yields no draws`() {
        val draws = SampleLotteryRemoteDataSource.parseHistory("""{"value":{"list":[]}}""", LotteryGame.DLT)
        assertEquals(emptyList<DrawResult>(), draws)
    }
}
