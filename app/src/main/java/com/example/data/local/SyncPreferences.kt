package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "loto_sync_prefs")

data class SyncMetadata(
  val lastSyncMillis: Long? = null,
  val lastDiscardedCount: Int = 0,
  val lastDuplicatesCount: Int = 0,
)

class SyncPreferences(private val context: Context) {

  private object Keys {
    val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
    val LAST_DISCARDED = intPreferencesKey("last_discarded_count")
    val LAST_DUPLICATES = intPreferencesKey("last_duplicates_count")
  }

  val syncMetadataFlow: Flow<SyncMetadata> =
    context.dataStore.data.map { prefs ->
      SyncMetadata(
        lastSyncMillis = prefs[Keys.LAST_SYNC_MILLIS],
        lastDiscardedCount = prefs[Keys.LAST_DISCARDED] ?: 0,
        lastDuplicatesCount = prefs[Keys.LAST_DUPLICATES] ?: 0,
      )
    }

  suspend fun saveSyncMetadata(timestampMillis: Long, discarded: Int, duplicates: Int) {
    context.dataStore.edit { prefs ->
      prefs[Keys.LAST_SYNC_MILLIS] = timestampMillis
      prefs[Keys.LAST_DISCARDED] = discarded
      prefs[Keys.LAST_DUPLICATES] = duplicates
    }
  }
}
