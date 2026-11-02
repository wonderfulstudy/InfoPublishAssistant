package com.jyf.infopublishassistant.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import kotlinx.coroutines.flow.Flow

/**
 * 复合主键 (game, issue) 让重复拉取同一期时 [Upsert] 天然去重，
 * 因此「进入页面先刷新第 1 页」不会产生重复行。
 */
@Entity(tableName = "draw_results", primaryKeys = ["game", "issue"])
data class DrawResultEntity(
    val game: String,
    val issue: String,
    val numbersEncoded: String,
)

fun DrawResultEntity.toDomain(): DrawResult =
    DrawResult(issue = issue, numbers = DrawNumbersCodec.decode(numbersEncoded))

fun DrawResult.toEntity(game: LotteryGame): DrawResultEntity =
    DrawResultEntity(
        game = game.name,
        issue = issue,
        numbersEncoded = DrawNumbersCodec.encode(numbers),
    )

@Dao
interface DrawResultDao {

    /**
     * 期号是「年份 + 当年序号」的定长数字串，按数值排序即为时间倒序；
     * 直接按文本排序在跨位数时会出错，所以显式 CAST。
     */
    @Query("SELECT * FROM draw_results WHERE game = :game ORDER BY CAST(issue AS INTEGER) DESC")
    fun observeByGame(game: String): Flow<List<DrawResultEntity>>

    @Query("SELECT COUNT(*) FROM draw_results WHERE game = :game")
    suspend fun countByGame(game: String): Int

    @Upsert
    suspend fun upsertAll(entities: List<DrawResultEntity>)
}

@Database(
    entities = [DrawResultEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class LotteryDatabase : RoomDatabase() {
    abstract fun drawResultDao(): DrawResultDao

    companion object {
        const val NAME = "lottery.db"
    }
}
