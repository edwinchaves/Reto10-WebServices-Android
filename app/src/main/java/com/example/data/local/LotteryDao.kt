package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LotteryDao {

  @Query("SELECT * FROM lottery_results ORDER BY drawDate DESC, year DESC, month DESC")
  fun observeAllDraws(): Flow<List<LotteryResultEntity>>

  @Query("SELECT COUNT(*) FROM lottery_results")
  suspend fun getDrawsCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDraws(draws: List<LotteryResultEntity>)

  @Query("DELETE FROM lottery_results")
  suspend fun clearAllDraws()

  @Query("SELECT * FROM favorite_combinations ORDER BY createdAtMillis DESC")
  fun observeFavorites(): Flow<List<FavoriteCombinationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFavorite(favorite: FavoriteCombinationEntity): Long

  @Query("DELETE FROM favorite_combinations WHERE id = :id")
  suspend fun deleteFavoriteById(id: Long)
}
