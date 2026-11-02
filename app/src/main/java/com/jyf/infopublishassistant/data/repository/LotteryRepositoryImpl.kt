package com.jyf.infopublishassistant.data.repository

import com.jyf.infopublishassistant.data.local.DrawResultDao
import com.jyf.infopublishassistant.data.local.toDomain
import com.jyf.infopublishassistant.data.local.toEntity
import com.jyf.infopublishassistant.data.remote.LotteryRemoteDataSource
import com.jyf.infopublishassistant.domain.model.DrawResult
import com.jyf.infopublishassistant.domain.model.LotteryGame
import com.jyf.infopublishassistant.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LotteryRepositoryImpl(
    private val remoteDataSource: LotteryRemoteDataSource,
    private val drawResultDao: DrawResultDao,
) : LotteryRepository {

    override fun observeDrawHistory(game: LotteryGame): Flow<List<DrawResult>> =
        drawResultDao.observeByGame(game.name).map { entities -> entities.map { it.toDomain() } }

    override suspend fun loadPage(game: LotteryGame, page: Int, pageSize: Int): Int {
        val fetched = remoteDataSource.fetchDrawHistory(game, page, pageSize)
        if (fetched.isEmpty()) return 0
        drawResultDao.upsertAll(fetched.map { it.toEntity(game) })
        return fetched.size
    }

    override suspend fun cachedCount(game: LotteryGame): Int =
        drawResultDao.countByGame(game.name)
}
