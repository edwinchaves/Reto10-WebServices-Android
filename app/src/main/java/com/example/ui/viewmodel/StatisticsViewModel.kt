package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LotteryRepository
import com.example.domain.statistics.AnalysisTarget
import com.example.domain.statistics.BellCurveAnalysis
import com.example.domain.statistics.FrequencySummary
import com.example.domain.statistics.StatisticalEngine
import com.example.domain.statistics.TemporalAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

enum class StatisticsSection(val label: String) {
  DESCRIPTIVE_BELL("Descriptiva y Campana"),
  FREQUENCIES("Frecuencias"),
  TEMPORAL("Temporal"),
}

data class StatisticsUiState(
  val activeSection: StatisticsSection = StatisticsSection.DESCRIPTIVE_BELL,
  val selectedTarget: AnalysisTarget = AnalysisTarget.WHOLE_NUMBER,
  val selectedLottery: String = "Todas",
  val availableLotteries: List<String> = listOf("Todas"),
  val availableYears: List<Int> = emptyList(),
  val selectedYearA: Int? = null,
  val selectedYearB: Int? = null,
  val totalDrawsAvailable: Int = 0,
  val bellCurveAnalysis: BellCurveAnalysis? = null,
  val frequencySummary: FrequencySummary? = null,
  val temporalAnalysis: TemporalAnalysis? = null,
)

class StatisticsViewModel(
  repository: LotteryRepository,
) : ViewModel() {

  private val sectionFlow = MutableStateFlow(StatisticsSection.DESCRIPTIVE_BELL)
  private val targetFlow = MutableStateFlow(AnalysisTarget.WHOLE_NUMBER)
  private val lotteryFlow = MutableStateFlow("Todas")
  private val yearComparisonFlow = MutableStateFlow<Pair<Int?, Int?>>(null to null)

  val uiState: StateFlow<StatisticsUiState> =
    combine(
      repository.cachedDrawsFlow,
      sectionFlow,
      targetFlow,
      lotteryFlow,
      yearComparisonFlow,
    ) { draws, section, target, lottery, yearPair ->
      val lotteries = listOf("Todas") + draws.map { it.loter_a }.distinct().sorted()
      val years = draws.mapNotNull { it.a_o_del_sorteo }.distinct().sortedDescending()

      if (draws.isEmpty()) {
        StatisticsUiState(
          activeSection = section,
          selectedTarget = target,
          selectedLottery = lottery,
          availableLotteries = lotteries,
          availableYears = years,
          totalDrawsAvailable = 0,
        )
      } else {
        val effectiveLottery = if (lottery in lotteries) lottery else "Todas"
        val bell = StatisticalEngine.analyzeBellCurve(draws, target, effectiveLottery)
        val freq = StatisticalEngine.computeFrequencySummary(draws, effectiveLottery)
        val temporal = StatisticalEngine.computeTemporalAnalysis(
          draws = draws,
          lotteryFilter = effectiveLottery,
          selectedYearA = yearPair.first,
          selectedYearB = yearPair.second,
        )

        StatisticsUiState(
          activeSection = section,
          selectedTarget = target,
          selectedLottery = effectiveLottery,
          availableLotteries = lotteries,
          availableYears = years,
          selectedYearA = temporal.comparison.yearA,
          selectedYearB = temporal.comparison.yearB,
          totalDrawsAvailable = draws.size,
          bellCurveAnalysis = bell,
          frequencySummary = freq,
          temporalAnalysis = temporal,
        )
      }
    }
      .flowOn(Dispatchers.Default)
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(),
      )

  fun selectSection(section: StatisticsSection) {
    sectionFlow.value = section
  }

  fun selectTarget(target: AnalysisTarget) {
    targetFlow.value = target
  }

  fun selectLottery(lottery: String) {
    lotteryFlow.value = lottery
  }

  fun selectComparisonYears(yearA: Int?, yearB: Int?) {
    yearComparisonFlow.value = yearA to yearB
  }

  class Factory(private val repository: LotteryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return StatisticsViewModel(repository) as T
    }
  }
}
