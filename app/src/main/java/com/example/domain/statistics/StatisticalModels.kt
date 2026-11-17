package com.example.domain.statistics

enum class AnalysisTarget(val label: String, val isDiscreteDigit: Boolean) {
  WHOLE_NUMBER("Número completo (0000–9999)", false),
  ALL_DIGITS("Todos los dígitos (0–9)", true),
  DIGIT_1("1er Dígito (Millar)", true),
  DIGIT_2("2do Dígito (Centena)", true),
  DIGIT_3("3er Dígito (Decena)", true),
  DIGIT_4("4to Dígito (Unidad)", true),
}

data class FrequencyItem(
  val label: String,
  val count: Int,
  val relativeFrequency: Double,
  val percentage: Double,
)

data class HistogramBin(
  val label: String,
  val rangeStart: Double,
  val rangeEnd: Double,
  val midpoint: Double,
  val observedCount: Int,
  val observedPct: Double,
  val theoreticalNormalCount: Double,
  val theoreticalNormalPct: Double,
)

data class DescriptiveStats(
  val sampleSize: Int,
  val mean: Double,
  val median: Double,
  val modes: List<String>,
  val modeFrequency: Int,
  val min: Double,
  val max: Double,
  val range: Double,
  val variance: Double,
  val stdDev: Double,
  val frequencyItems: List<FrequencyItem>,
)

data class BellCurveAnalysis(
  val target: AnalysisTarget,
  val stats: DescriptiveStats,
  val bins: List<HistogramBin>,
  val isNormalFitAppropriate: Boolean,
  val statisticalNoticeTitle: String,
  val statisticalNoticeBody: String,
  val withinOneSigmaObservedPct: Double,
  val withinTwoSigmaObservedPct: Double,
  val withinThreeSigmaObservedPct: Double,
  val skewness: Double,
)

data class PositionalDigitFrequency(
  val digit: Int,
  val pos1Count: Int,
  val pos2Count: Int,
  val pos3Count: Int,
  val pos4Count: Int,
  val totalCount: Int,
  val totalPct: Double,
)

data class FrequencySummary(
  val totalDrawsAnalyzed: Int,
  val totalDigitsAnalyzed: Int,
  val mostFrequentDigits: List<FrequencyItem>,
  val leastFrequentDigits: List<FrequencyItem>,
  val topTwoDigitCombinations: List<FrequencyItem>,
  val topThreeDigitCombinations: List<FrequencyItem>,
  val topLastOneDigitEndings: List<FrequencyItem>,
  val topLastTwoDigitEndings: List<FrequencyItem>,
  val topLastThreeDigitEndings: List<FrequencyItem>,
  val positionalMatrix: List<PositionalDigitFrequency>,
  val repeatedFullTickets: List<FrequencyItem>,
  val drawsByLottery: List<FrequencyItem>,
  val drawsByYear: List<FrequencyItem>,
  val drawsByMonth: List<FrequencyItem>,
)

data class YearTemporalPoint(
  val year: Int,
  val drawCount: Int,
  val meanTicketValue: Double,
  val topDigit: Int,
  val topDigitPct: Double,
)

data class MonthTemporalPoint(
  val month: Int,
  val monthLabel: String,
  val drawCount: Int,
  val meanTicketValue: Double,
)

data class TemporalComparison(
  val yearA: Int?,
  val yearB: Int?,
  val yearADigitPcts: List<Double>,
  val yearBDigitPcts: List<Double>,
  val yearADrawCount: Int,
  val yearBDrawCount: Int,
)

data class TemporalAnalysis(
  val yearlyEvolution: List<YearTemporalPoint>,
  val monthlyDistribution: List<MonthTemporalPoint>,
  val comparison: TemporalComparison,
)
