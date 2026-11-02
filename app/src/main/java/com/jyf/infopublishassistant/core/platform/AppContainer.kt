package com.jyf.infopublishassistant.core.platform

import android.content.Context
import androidx.room.Room
import com.jyf.infopublishassistant.core.display.DisplaySettingsStore
import com.jyf.infopublishassistant.data.local.LotteryDatabase
import com.jyf.infopublishassistant.data.local.settingsDataStore
import com.jyf.infopublishassistant.data.remote.LotteryRemoteDataSource
import com.jyf.infopublishassistant.data.remote.SampleLotteryRemoteDataSource
import com.jyf.infopublishassistant.data.repository.LotteryRepositoryImpl
import com.jyf.infopublishassistant.domain.repository.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 手写依赖容器（本项目不使用 Hilt）。
 *
 * 所有依赖都是 lazy 的，只有真正被访问时才创建；
 * 由 [com.jyf.infopublishassistant.InfoPublishApp] 持有唯一实例。
 */
class AppContainer(private val context: Context) {

    /** 进程级作用域，用于承载不随页面生命周期结束的设置监听。 */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val displaySettingsStore: DisplaySettingsStore by lazy {
        DisplaySettingsStore(context.settingsDataStore, applicationScope)
    }

    private val database: LotteryDatabase by lazy {
        Room.databaseBuilder(context, LotteryDatabase::class.java, LotteryDatabase.NAME).build()
    }

    /**
     * TODO(接口实现): 把这里换成你自己的 [LotteryRemoteDataSource] 实现
     *   （Retrofit / OkHttp / Ktor 均可）。替换后 UI 层与缓存层都无需改动。
     */
    private val remoteDataSource: LotteryRemoteDataSource by lazy {
        SampleLotteryRemoteDataSource()
    }

    val lotteryRepository: LotteryRepository by lazy {
        LotteryRepositoryImpl(remoteDataSource, database.drawResultDao())
    }
}
