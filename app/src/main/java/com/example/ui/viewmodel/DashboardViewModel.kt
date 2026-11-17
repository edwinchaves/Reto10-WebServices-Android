package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LotteryDrawResult
import com.example.data.model.SyncState
import com.example.data.repository.LotteryRepository
import com.example.domain.statistics.AnalysisTarget
import com.example.domain.statistics.BellCurveAnalysis
import com.example.domain.statistics.FrequencyItem
import com.example.domain.statistics.StatisticalEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
  val totalRecords: Int = 0,
  val lastSyncMillis: Long? = null,
  val discardedRecords: Int = 0,
  val duplicatesRemoved: Int = 0,
  val latestDraw: LotteryDrawResult? = null,
  val availableLotteries: List<String> = emptyList(),
  val digitDistribution: List<FrequencyItem> = emptyList(),
  val mostFrequentDigits: List<FrequencyItem> = emptyList(),
  val leastFrequentDigits: List<FrequencyItem> = emptyList(),
  val topTwoDigitEndings: List<FrequencyItem> = emptyList(),
  val wholeNumberOverview: BellCurveAnalysis? = null,
  val syncState: SyncState = SyncState.Idle,
)

class DashboardViewModel(
  private val repository: LotteryRepository,
) : ViewModel() {

  private val syncStateFlow = MutableStateFlow<SyncState>(SyncState.Idle)
  private var pagesSynced = 2

  val uiState: StateFlow<DashboardUiState> =
    combine(
      repository.cachedDrawsFlow,
      repository.syncMetadataFlow,
      syncStateFlow,
    ) { draws, syncMeta, syncState ->
      if (draws.isEmpty()) {
        DashboardUiState(
          totalRecords = 0,
          lastSyncMillis = syncMeta.lastSyncMillis,
          discardedRecords = syncMeta.lastDiscardedCount,
          duplicatesRemoved = syncMeta.lastDuplicatesCount,
          syncState = syncState,
        )
      } else {
        val freq = StatisticalEngine.computeFrequencySummary(draws)
        val bell = StatisticalEngine.analyzeBellCurve(draws, AnalysisTarget.WHOLE_NUMBER)
        val lotteries = draws.map { it.loter_a }.distinct().sorted()
        val latest = draws.maxByOrNull { it.formattedDate }

        DashboardUiState(
          totalRecords = draws.size,
          lastSyncMillis = syncMeta.lastSyncMillis,
          discardedRecords = syncMeta.lastDiscardedCount,
          duplicatesRemoved = syncMeta.lastDuplicatesCount,
          latestDraw = latest,
          availableLotteries = lotteries,
          digitDistribution = freq.positionalMatrix.map {
            FrequencyItem(
              label = it.digit.toString(),
              count = it.totalCount,
              relativeFrequency = it.totalPct / 100.0,
              percentage = it.totalPct,
            )
          },
          mostFrequentDigits = freq.mostFrequentDigits.take(4),
          leastFrequentDigits = freq.leastFrequentDigits.take(4),
          topTwoDigitEndings = freq.topLastTwoDigitEndings.take(5),
          wholeNumberOverview = bell,
          syncState = syncState,
        )
      }
    }
      .flowOn(Dispatchers.Default)
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
      )

  init {
    viewModelScope.launch {
      if (repository.getLocalCount() == 0) {
        synchronizeData()
      }
    }
  }

  fun synchronizeData(fetchMorePages: Boolean = false) {
    if (syncStateFlow.value is SyncState.Loading) return
    if (fetchMorePages) {
      pagesSynced = (pagesSynced + 2).coerceAtMost(8)
    }
    viewModelScope.launch {
      syncStateFlow.value = SyncState.Loading("Consultando datos.gov.co…")
      val result = repository.synchronizeHistoricalData(pagesToFetch = pagesSynced, pageSize = 1000)
      syncStateFlow.value = result.fold(
        onSuccess = { summary ->
          SyncState.Success(
            recordsCount = summary.totalFetched,
            timestampMillis = summary.timestampMillis,
          )
        },
        onFailure = { error ->
          SyncState.Error(
            error.localizedMessage ?: "No se pudo sincronizar con datos.gov.co."
          )
        },
      )
    }
  }

  fun dismissSyncStatus() {
    if (syncStateFlow.value !is SyncState.Loading) {
      syncStateFlow.value = SyncState.Idle
    }
  }

  class Factory(private val repository: LotteryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return DashboardViewModel(repository) as T
    }
  }
}
