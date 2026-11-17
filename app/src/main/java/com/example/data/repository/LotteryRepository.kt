package com.example.data.repository

import com.example.data.local.LotteryDao
import com.example.data.local.SyncMetadata
import com.example.data.local.SyncPreferences
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.model.ApiQueryParams
import com.example.data.model.FavoriteCombination
import com.example.data.model.LotteryDrawResult
import com.example.data.remote.DatosGovLotteryService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SyncSummary(
  val totalFetched: Int,
  val discardedCount: Int,
  val duplicatesCount: Int,
  val timestampMillis: Long,
)

class LotteryRepository(
  private val apiService: DatosGovLotteryService,
  private val lotteryDao: LotteryDao,
  private val syncPreferences: SyncPreferences,
) {

  val cachedDrawsFlow: Flow<List<LotteryDrawResult>> =
    lotteryDao.observeAllDraws().map { list -> list.map { it.toDomain() } }

  val favoritesFlow: Flow<List<FavoriteCombination>> =
    lotteryDao.observeFavorites().map { list -> list.map { it.toDomain() } }

  val syncMetadataFlow: Flow<SyncMetadata> = syncPreferences.syncMetadataFlow

  /**
   * Descarga registros paginados desde datos.gov.co (por lotes de 1000 usando $limit y $offset)
   * y actualiza la base local Room para análisis estadístico sin conexión tras sincronizar.
   */
  suspend fun synchronizeHistoricalData(pagesToFetch: Int = 2, pageSize: Int = 1000): Result<SyncSummary> {
    val allValid = LinkedHashMap<String, LotteryDrawResult>()
    var totalDiscarded = 0
    var totalDuplicates = 0

    for (page in 0 until pagesToFetch) {
      val offset = page * pageSize
      val result = apiService.fetchResults(
        ApiQueryParams(
          limit = pageSize,
          offset = offset,
          sortDescending = true,
        )
      )

      if (result.isFailure) {
        // Si ya obtuvimos al menos una página completa, guardamos lo descargado antes de reportar si falló la primera
        if (page == 0) {
          return Result.failure(result.exceptionOrNull() ?: IllegalStateException("Error de sincronización"))
        } else {
          break
        }
      }

      val report = result.getOrThrow()
      totalDiscarded += report.discardedIncomplete
      totalDuplicates += report.duplicatesRemoved

      for (record in report.validRecords) {
        if (allValid.containsKey(record.uniqueId)) {
          totalDuplicates++
        } else {
          allValid[record.uniqueId] = record
        }
      }

      if (report.rawCount < pageSize) {
        break
      }
    }

    val validList = allValid.values.toList()
    if (validList.isEmpty()) {
      return Result.failure(
        IllegalStateException("El servicio web no devolvió registros válidos para procesar.")
      )
    }

    lotteryDao.insertDraws(validList.map { it.toEntity() })
    val now = System.currentTimeMillis()
    syncPreferences.saveSyncMetadata(
      timestampMillis = now,
      discarded = totalDiscarded,
      duplicates = totalDuplicates,
    )

    return Result.success(
      SyncSummary(
        totalFetched = validList.size,
        discardedCount = totalDiscarded,
        duplicatesCount = totalDuplicates,
        timestampMillis = now,
      )
    )
  }

  /**
   * Consulta directamente el endpoint oficial de datos.gov.co con filtros SoQL reales.
   * También guarda los registros consultados en Room para enriquecer el repositorio local.
   */
  suspend fun queryRemoteDraws(params: ApiQueryParams): Result<List<LotteryDrawResult>> {
    val result = apiService.fetchResults(params)
    return result.map { report ->
      if (report.validRecords.isNotEmpty()) {
        lotteryDao.insertDraws(report.validRecords.map { it.toEntity() })
      }
      report.validRecords
    }
  }

  suspend fun getLocalCount(): Int = lotteryDao.getDrawsCount()

  suspend fun saveFavorite(combination: FavoriteCombination): Long {
    return lotteryDao.insertFavorite(combination.toEntity())
  }

  suspend fun deleteFavorite(id: Long) {
    lotteryDao.deleteFavoriteById(id)
  }
}
