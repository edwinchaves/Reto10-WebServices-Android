package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [LotteryResultEntity::class, FavoriteCombinationEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class LotoDatabase : RoomDatabase() {

  abstract fun lotteryDao(): LotteryDao

  companion object {
    @Volatile
    private var INSTANCE: LotoDatabase? = null

    fun getInstance(context: Context): LotoDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          LotoDatabase::class.java,
          "loto_analytics_db",
        ).build().also { INSTANCE = it }
      }
    }
  }
}
