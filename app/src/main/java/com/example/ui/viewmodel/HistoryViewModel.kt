package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LotteryDrawResult
import com.example.data.repository.LotteryRepository
import com.example.domain.history.ComparisonEngine
import com.example.domain.history.FavoriteComparisonSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryTab(val label: String) {
  FAVORITES_COMPARE("Favoritas y Comparación"),
  HISTORICAL_LOG("Sorteos Anteriores"),
}

data class HistoryUiState(
  val activeTab: HistoryTab = HistoryTab.FAVORITES_COMPARE,
  val comparisons: List<FavoriteComparisonSummary> = emptyList(),
  val historicalDraws: List<LotteryDrawResult> = emptyList(),
  val searchNumberQuery: String = "",
  val feedbackMessage: String? = null,
)

class HistoryViewModel(
  private val repository: LotteryRepository,
) : ViewModel() {

  private val activeTabFlow = MutableStateFlow(HistoryTab.FAVORITES_COMPARE)
  private val searchQueryFlow = MutableStateFlow("")
  private val feedbackFlow = MutableStateFlow<String?>(null)

  val uiState: StateFlow<HistoryUiState> =
    combine(
      repository.favoritesFlow,
      repository.cachedDrawsFlow,
      activeTabFlow,
      searchQueryFlow,
      feedbackFlow,
    ) { favorites, draws, tab, query, feedback ->
      val comparisons = favorites.map { fav ->
        ComparisonEngine.compareFavoriteWithHistory(fav, draws)
      }
      val cleanQuery = query.trim()
      val filteredDraws = if (cleanQuery.isEmpty()) {
        draws.take(150)
      } else {
        draws.filter {
          it.numero_billete_ganador.contains(cleanQuery, ignoreCase = true) ||
            it.loter_a.contains(cleanQuery, ignoreCase = true) ||
            it.n_mero_del_sorteo.contains(cleanQuery, ignoreCase = true) ||
            it.fecha_del_sorteo.contains(cleanQuery, ignoreCase = true)
        }.take(150)
      }

      HistoryUiState(
        activeTab = tab,
        comparisons = comparisons,
        historicalDraws = filteredDraws,
        searchNumberQuery = query,
        feedbackMessage = feedback,
      )
    }
      .flowOn(Dispatchers.Default)
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
      )

  fun selectTab(tab: HistoryTab) {
    activeTabFlow.value = tab
  }

  fun updateSearchQuery(query: String) {
    searchQueryFlow.value = query
  }

  fun deleteFavorite(id: Long) {
    viewModelScope.launch {
      repository.deleteFavorite(id)
      feedbackFlow.value = "Combinación eliminada de favoritas."
    }
  }

  fun clearFeedback() {
    feedbackFlow.value = null
  }

  class Factory(private val repository: LotteryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HistoryViewModel(repository) as T
    }
  }
}
