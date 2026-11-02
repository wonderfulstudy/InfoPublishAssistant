package com.jyf.infopublishassistant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/** 应用级设置存储。委托属性保证整个进程只有一个 DataStore 实例。 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
