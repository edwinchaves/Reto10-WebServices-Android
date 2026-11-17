package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApiQueryParams
import com.example.data.model.LotteryDrawResult
import com.example.data.model.RemoteQueryState
import com.example.data.repository.LotteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResultsFilterState(
  val year: Int? = null,
  val month: Int? = null,
  val lotteryName: String = "",
  val dateFilter: String = "",
  val prizeType: String = "",
  val searchQuery: String = "",
  val sortDescending: Boolean = true,
  val page: Int = 0,
  val pageSize: Int = 30,
)

data class ResultsUiState(
  val filters: ResultsFilterState = ResultsFilterState(),
  val availableYears: List<Int> = emptyList(),
  val availableLotteries: List<String> = emptyList(),
  val availablePrizeTypes: List<String> = emptyList(),
  val queryState: RemoteQueryState<List<LotteryDrawResult>> = RemoteQueryState.Idle,
)

class ResultsViewModel(
  private val repository: LotteryRepository,
) : ViewModel() {

  private val filtersFlow = MutableStateFlow(ResultsFilterState())
  private val queryStateFlow =
    MutableStateFlow<RemoteQueryState<List<LotteryDrawResult>>>(RemoteQueryState.Idle)

  val uiState: StateFlow<ResultsUiState> =
    combine(
      filtersFlow,
      queryStateFlow,
      repository.cachedDrawsFlow,
    ) { filters, queryState, cachedDraws ->
      val years = cachedDraws.mapNotNull { it.a_o_del_sorteo }.distinct().sortedDescending()
      val lotteries = cachedDraws.map { it.loter_a }.distinct().sorted()
      val prizes = cachedDraws.map { it.tipo_de_premio }.distinct().sorted()
      ResultsUiState(
        filters = filters,
        availableYears = years,
        availableLotteries = lotteries,
        availablePrizeTypes = prizes,
        queryState = queryState,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = ResultsUiState(),
    )

  init {
    executeQuery()
  }

  fun updateYear(year: Int?) {
    filtersFlow.value = filtersFlow.value.copy(year = year, page = 0)
    executeQuery()
  }

  fun updateMonth(month: Int?) {
    filtersFlow.value = filtersFlow.value.copy(month = month, page = 0)
    executeQuery()
  }

  fun updateLottery(lottery: String) {
    filtersFlow.value = filtersFlow.value.copy(lotteryName = lottery, page = 0)
    executeQuery()
  }

  fun updatePrizeType(prizeType: String) {
    filtersFlow.value = filtersFlow.value.copy(prizeType = prizeType, page = 0)
    executeQuery()
  }

  fun updateDateFilter(dateText: String) {
    filtersFlow.value = filtersFlow.value.copy(dateFilter = dateText, page = 0)
  }

  fun updateSearchQuery(search: String) {
    filtersFlow.value = filtersFlow.value.copy(searchQuery = search, page = 0)
  }

  fun toggleSortOrder() {
    val current = filtersFlow.value
    filtersFlow.value = current.copy(sortDescending = !current.sortDescending, page = 0)
    executeQuery()
  }

  fun clearFilters() {
    filtersFlow.value = ResultsFilterState()
    executeQuery()
  }

  fun nextPage() {
    val state = queryStateFlow.value
    if (state is RemoteQueryState.Success && state.hasMore) {
      filtersFlow.value = filtersFlow.value.copy(page = filtersFlow.value.page + 1)
      executeQuery()
    }
  }

  fun previousPage() {
    val current = filtersFlow.value
    if (current.page > 0) {
      filtersFlow.value = current.copy(page = current.page - 1)
      executeQuery()
    }
  }

  fun executeQuery() {
    val f = filtersFlow.value
    viewModelScope.launch {
      queryStateFlow.value = RemoteQueryState.Loading
      val params = ApiQueryParams(
        limit = f.pageSize,
        offset = f.page * f.pageSize,
        year = f.year,
        month = f.month,
        lotteryName = f.lotteryName.ifBlank { null },
        prizeType = f.prizeType.ifBlank { null },
        dateFilter = f.dateFilter.ifBlank { null },
        searchQuery = f.searchQuery.ifBlank { null },
        sortDescending = f.sortDescending,
      )

      val result = repository.queryRemoteDraws(params)
      queryStateFlow.value = result.fold(
        onSuccess = { list ->
          RemoteQueryState.Success(
            data = list,
            hasMore = list.size >= f.pageSize,
          )
        },
        onFailure = { error ->
          RemoteQueryState.Error(
            error.localizedMessage ?: "Error al consultar resultados en datos.gov.co."
          )
        },
      )
    }
  }

  class Factory(private val repository: LotteryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return ResultsViewModel(repository) as T
    }
  }
}
