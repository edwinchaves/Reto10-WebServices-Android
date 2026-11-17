package com.example.domain.generator

enum class ChanceModality(val digitsCount: Int, val label: String) {
  THREE_DIGITS(3, "Chance 3 Cifras"),
  FOUR_DIGITS(4, "Chance 4 Cifras"),
}

enum class GenerationStrategy(
  val title: String,
  val shortTag: String,
  val description: String,
) {
  HISTORICAL_FREQUENCY(
    title = "Estrategia A: Frecuencia histórica",
    shortTag = "Frec. Histórica",
    description = "Pondera cada dígito según su frecuencia posicional observada en el historial seleccionado.",
  ),
  RECENT_FREQUENCY(
    title = "Estrategia B: Frecuencia reciente",
    shortTag = "Frec. Reciente",
    description = "Asigna mayor peso descriptivo a los sorteos más recientes dentro de la ventana temporal elegida.",
  ),
  MIXED(
    title = "Estrategia C: Combinaciones mixtas",
    shortTag = "Mixta (A/M/B)",
    description = "Equilibra dígitos de frecuencia histórica alta, media y baja en distintas posiciones.",
  ),
  RANDOM(
    title = "Estrategia D: Generación aleatoria",
    shortTag = "Aleatoria",
    description = "Muestreo uniforme independiente entre 0 y 9 por posición, conservando ceros iniciales.",
  ),
}

enum class HistoricalWindow(val label: String, val monthsBack: Int?) {
  ALL("Todo el historial", null),
  LAST_3_MONTHS("Últimos 3 meses", 3),
  LAST_6_MONTHS("Últimos 6 meses", 6),
  LAST_1_YEAR("Último año", 12),
  LAST_2_YEARS("Últimos 2 años", 24),
}

data class DigitPositionStat(
  val positionIndex: Int,
  val digit: Int,
  val positionalOccurrences: Int,
  val positionalPct: Double,
  val tierLabel: String,
)

data class GeneratedCombination(
  val id: String,
  val number: String,
  val modality: ChanceModality,
  val strategy: GenerationStrategy,
  val referenceLottery: String,
  val window: HistoricalWindow,
  val sampleSizeUsed: Int,
  val digitStats: List<DigitPositionStat>,
  val exactHistoricalMatches: Int,
  val endingTwoDigitMatches: Int,
  val justification: String,
)
