package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FavoriteCombination
import com.example.data.model.LotteryDrawResult
import com.example.data.repository.LotteryRepository
import com.example.domain.generator.ChanceGeneratorEngine
import com.example.domain.generator.ChanceModality
import com.example.domain.generator.GeneratedCombination
import com.example.domain.generator.GenerationStrategy
import com.example.domain.generator.HistoricalWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GeneratorUiState(
  val modality: ChanceModality = ChanceModality.FOUR_DIGITS,
  val strategy: GenerationStrategy = GenerationStrategy.HISTORICAL_FREQUENCY,
  val referenceLottery: String = "Todas",
  val historicalWindow: HistoricalWindow = HistoricalWindow.ALL,
  val quantity: Int = 4,
  val availableLotteries: List<String> = listOf("Todas"),
  val totalAvailableDraws: Int = 0,
  val generatedCombinations: List<GeneratedCombination> = emptyList(),
  val savedNumbersSet: Set<String> = emptySet(),
  val isGenerating: Boolean = false,
  val feedbackMessage: String? = null,
)

class GeneratorViewModel(
  private val repository: LotteryRepository,
) : ViewModel() {

  private val modalityFlow = MutableStateFlow(ChanceModality.FOUR_DIGITS)
  private val strategyFlow = MutableStateFlow(GenerationStrategy.HISTORICAL_FREQUENCY)
  private val lotteryFlow = MutableStateFlow("Todas")
  private val windowFlow = MutableStateFlow(HistoricalWindow.ALL)
  private val quantityFlow = MutableStateFlow(4)
  private val generatedFlow = MutableStateFlow<List<GeneratedCombination>>(emptyList())
  private val isGeneratingFlow = MutableStateFlow(false)
  private val feedbackFlow = MutableStateFlow<String?>(null)

  private var latestDrawsSnapshot: List<LotteryDrawResult> = emptyList()

  private data class ConfigBundle(
    val modality: ChanceModality,
    val strategy: GenerationStrategy,
    val lottery: String,
    val window: HistoricalWindow,
    val quantity: Int,
  )

  private val configFlow = combine(
    modalityFlow,
    strategyFlow,
    lotteryFlow,
    windowFlow,
    quantityFlow,
  ) { m, s, l, w, q -> ConfigBundle(m, s, l, w, q) }

  val uiState: StateFlow<GeneratorUiState> =
    combine(
      repository.cachedDrawsFlow,
      repository.favoritesFlow,
      configFlow,
      generatedFlow,
      combine(isGeneratingFlow, feedbackFlow) { gen, msg -> gen to msg },
    ) { draws, favorites, cfg, generated, status ->
      latestDrawsSnapshot = draws
      val lotteries = listOf("Todas") + draws.map { it.loter_a }.distinct().sorted()
      val savedSet = favorites.map { "${it.digitsCount}:${it.number}" }.toSet()
      GeneratorUiState(
        modality = cfg.modality,
        strategy = cfg.strategy,
        referenceLottery = if (cfg.lottery in lotteries) cfg.lottery else "Todas",
        historicalWindow = cfg.window,
        quantity = cfg.quantity,
        availableLotteries = lotteries,
        totalAvailableDraws = draws.size,
        generatedCombinations = generated,
        savedNumbersSet = savedSet,
        isGenerating = status.first,
        feedbackMessage = status.second,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = GeneratorUiState(),
    )

  fun setModality(modality: ChanceModality) {
    modalityFlow.value = modality
  }

  fun setStrategy(strategy: GenerationStrategy) {
    strategyFlow.value = strategy
  }

  fun setReferenceLottery(lottery: String) {
    lotteryFlow.value = lottery
  }

  fun setHistoricalWindow(window: HistoricalWindow) {
    windowFlow.value = window
  }

  fun setQuantity(quantity: Int) {
    quantityFlow.value = quantity.coerceIn(1, 10)
  }

  fun generate() {
    val draws = latestDrawsSnapshot
    if (draws.isEmpty() && strategyFlow.value != GenerationStrategy.RANDOM) {
      feedbackFlow.value = "Sincroniza primero los sorteos en Inicio para usar estrategias históricas."
      return
    }
    viewModelScope.launch {
      isGeneratingFlow.value = true
      val results = withContext(Dispatchers.Default) {
        ChanceGeneratorEngine.generateCombinations(
          draws = draws,
          modality = modalityFlow.value,
          strategy = strategyFlow.value,
          referenceLottery = lotteryFlow.value,
          window = windowFlow.value,
          quantity = quantityFlow.value,
        )
      }
      generatedFlow.value = results
      isGeneratingFlow.value = false
    }
  }

  fun saveToFavorites(combination: GeneratedCombination) {
    viewModelScope.launch {
      val digitSummary = combination.digitStats.joinToString(" | ") {
        "${it.digit} (${"%.1f".format(it.positionalPct)}%)"
      }
      repository.saveFavorite(
        FavoriteCombination(
          number = combination.number,
          digitsCount = combination.modality.digitsCount,
          strategyName = combination.strategy.shortTag,
          referenceLottery = combination.referenceLottery,
          justification = combination.justification,
          digitFrequencySummary = digitSummary,
          createdAtMillis = System.currentTimeMillis(),
        )
      )
      feedbackFlow.value = "Combinación ${combination.number} guardada en favoritas."
    }
  }

  fun clearFeedback() {
    feedbackFlow.value = null
  }

  class Factory(private val repository: LotteryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return GeneratorViewModel(repository) as T
    }
  }
}
