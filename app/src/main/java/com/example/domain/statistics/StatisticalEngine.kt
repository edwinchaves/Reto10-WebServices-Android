package com.example.domain.statistics

import com.example.data.model.LotteryDrawResult
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

object StatisticalEngine {

  private val SPANISH_MONTHS = listOf(
    "Ene", "Feb", "Mar", "Abr", "May", "Jun",
    "Jul", "Ago", "Sep", "Oct", "Nov", "Dic",
  )

  fun analyzeBellCurve(
    draws: List<LotteryDrawResult>,
    target: AnalysisTarget,
    lotteryFilter: String? = null,
  ): BellCurveAnalysis {
    val filtered = filterByLottery(draws, lotteryFilter)
    val values = extractValuesForTarget(filtered, target)

    if (values.isEmpty()) {
      val emptyStats = DescriptiveStats(
        sampleSize = 0,
        mean = 0.0,
        median = 0.0,
        modes = emptyList(),
        modeFrequency = 0,
        min = 0.0,
        max = 0.0,
        range = 0.0,
        variance = 0.0,
        stdDev = 0.0,
        frequencyItems = emptyList(),
      )
      return BellCurveAnalysis(
        target = target,
        stats = emptyStats,
        bins = emptyList(),
        isNormalFitAppropriate = false,
        statisticalNoticeTitle = "Sin datos suficientes",
        statisticalNoticeBody = "Sincroniza resultados o ajusta el filtro de lotería para calcular estadísticas.",
        withinOneSigmaObservedPct = 0.0,
        withinTwoSigmaObservedPct = 0.0,
        withinThreeSigmaObservedPct = 0.0,
        skewness = 0.0,
      )
    }

    val stats = computeDescriptiveStats(values, target)
    val skewness = computeSkewness(values, stats.mean, stats.stdDev)

    val bins = if (target.isDiscreteDigit) {
      buildDigitHistogramBins(values, stats.mean, stats.stdDev)
    } else {
      buildWholeNumberHistogramBins(values, stats.mean, stats.stdDev, binCount = 10)
    }

    val (within1, within2, within3) = computeSigmaCoverage(values, stats.mean, stats.stdDev)

    val (isAppropriate, noticeTitle, noticeBody) = evaluateNormalSuitability(
      target = target,
      sampleSize = values.size,
      stdDev = stats.stdDev,
      skewness = skewness,
    )

    return BellCurveAnalysis(
      target = target,
      stats = stats,
      bins = bins,
      isNormalFitAppropriate = isAppropriate,
      statisticalNoticeTitle = noticeTitle,
      statisticalNoticeBody = noticeBody,
      withinOneSigmaObservedPct = within1,
      withinTwoSigmaObservedPct = within2,
      withinThreeSigmaObservedPct = within3,
      skewness = skewness,
    )
  }

  fun computeFrequencySummary(
    draws: List<LotteryDrawResult>,
    lotteryFilter: String? = null,
  ): FrequencySummary {
    val filtered = filterByLottery(draws, lotteryFilter)
    if (filtered.isEmpty()) {
      return FrequencySummary(
        totalDrawsAnalyzed = 0,
        totalDigitsAnalyzed = 0,
        mostFrequentDigits = emptyList(),
        leastFrequentDigits = emptyList(),
        topTwoDigitCombinations = emptyList(),
        topThreeDigitCombinations = emptyList(),
        topLastOneDigitEndings = emptyList(),
        topLastTwoDigitEndings = emptyList(),
        topLastThreeDigitEndings = emptyList(),
        positionalMatrix = emptyList(),
        repeatedFullTickets = emptyList(),
        drawsByLottery = emptyList(),
        drawsByYear = emptyList(),
        drawsByMonth = emptyList(),
      )
    }

    val posCounts = Array(4) { IntArray(10) }
    val totalDigitCounts = IntArray(10)
    val twoDigitSubstrings = HashMap<String, Int>()
    val threeDigitSubstrings = HashMap<String, Int>()
    val lastOneCounts = HashMap<String, Int>()
    val lastTwoCounts = HashMap<String, Int>()
    val lastThreeCounts = HashMap<String, Int>()
    val fullTicketCounts = HashMap<String, Int>()
    val lotteryCounts = HashMap<String, Int>()
    val yearCounts = HashMap<Int, Int>()
    val monthCounts = HashMap<Int, Int>()

    var totalDigits = 0

    for (draw in filtered) {
      val ticket4 = draw.fourDigitTicket
      if (ticket4.length == 4) {
        for (pos in 0 until 4) {
          val d = ticket4[pos].digitToIntOrNull()
          if (d != null) {
            posCounts[pos][d]++
            totalDigitCounts[d]++
            totalDigits++
          }
        }

        // Subcadenas de 2 cifras dentro del billete (pos 0-1, 1-2, 2-3)
        for (i in 0..2) {
          val pair = ticket4.substring(i, i + 2)
          twoDigitSubstrings[pair] = (twoDigitSubstrings[pair] ?: 0) + 1
        }
        // Subcadenas de 3 cifras (pos 0-2 y 1-3)
        for (i in 0..1) {
          val triple = ticket4.substring(i, i + 3)
          threeDigitSubstrings[triple] = (threeDigitSubstrings[triple] ?: 0) + 1
        }

        val last1 = ticket4.takeLast(1)
        val last2 = ticket4.takeLast(2)
        val last3 = ticket4.takeLast(3)
        lastOneCounts[last1] = (lastOneCounts[last1] ?: 0) + 1
        lastTwoCounts[last2] = (lastTwoCounts[last2] ?: 0) + 1
        lastThreeCounts[last3] = (lastThreeCounts[last3] ?: 0) + 1
        fullTicketCounts[ticket4] = (fullTicketCounts[ticket4] ?: 0) + 1
      }

      lotteryCounts[draw.loter_a] = (lotteryCounts[draw.loter_a] ?: 0) + 1
      draw.a_o_del_sorteo?.let { y -> yearCounts[y] = (yearCounts[y] ?: 0) + 1 }
      draw.mes_del_sorteo?.takeIf { it in 1..12 }?.let { m ->
        monthCounts[m] = (monthCounts[m] ?: 0) + 1
      }
    }

    val digitFreqItems = (0..9).map { d ->
      val count = totalDigitCounts[d]
      val rel = if (totalDigits > 0) count.toDouble() / totalDigits else 0.0
      FrequencyItem(
        label = d.toString(),
        count = count,
        relativeFrequency = rel,
        percentage = rel * 100.0,
      )
    }

    val positionalMatrix = (0..9).map { d ->
      val total = totalDigitCounts[d]
      val pct = if (totalDigits > 0) (total.toDouble() / totalDigits) * 100.0 else 0.0
      PositionalDigitFrequency(
        digit = d,
        pos1Count = posCounts[0][d],
        pos2Count = posCounts[1][d],
        pos3Count = posCounts[2][d],
        pos4Count = posCounts[3][d],
        totalCount = total,
        totalPct = pct,
      )
    }

    val twoDigitTotal = twoDigitSubstrings.values.sum().coerceAtLeast(1)
    val threeDigitTotal = threeDigitSubstrings.values.sum().coerceAtLeast(1)
    val drawsTotal = filtered.size.coerceAtLeast(1)

    return FrequencySummary(
      totalDrawsAnalyzed = filtered.size,
      totalDigitsAnalyzed = totalDigits,
      mostFrequentDigits = digitFreqItems.sortedByDescending { it.count },
      leastFrequentDigits = digitFreqItems.sortedBy { it.count },
      topTwoDigitCombinations = mapToTopFrequencyItems(twoDigitSubstrings, twoDigitTotal, limit = 12),
      topThreeDigitCombinations = mapToTopFrequencyItems(threeDigitSubstrings, threeDigitTotal, limit = 12),
      topLastOneDigitEndings = mapToTopFrequencyItems(lastOneCounts, drawsTotal, limit = 10),
      topLastTwoDigitEndings = mapToTopFrequencyItems(lastTwoCounts, drawsTotal, limit = 12),
      topLastThreeDigitEndings = mapToTopFrequencyItems(lastThreeCounts, drawsTotal, limit = 12),
      positionalMatrix = positionalMatrix,
      repeatedFullTickets = mapToTopFrequencyItems(fullTicketCounts, drawsTotal, limit = 15)
        .filter { it.count > 1 }
        .ifEmpty { mapToTopFrequencyItems(fullTicketCounts, drawsTotal, limit = 10) },
      drawsByLottery = mapToTopFrequencyItems(lotteryCounts, drawsTotal, limit = 25),
      drawsByYear = yearCounts.entries
        .sortedByDescending { it.key }
        .map { (y, c) ->
          val rel = c.toDouble() / drawsTotal
          FrequencyItem(y.toString(), c, rel, rel * 100.0)
        },
      drawsByMonth = (1..12).mapNotNull { m ->
        val c = monthCounts[m] ?: return@mapNotNull null
        val rel = c.toDouble() / drawsTotal
        FrequencyItem(SPANISH_MONTHS[m - 1], c, rel, rel * 100.0)
      },
    )
  }

  fun computeTemporalAnalysis(
    draws: List<LotteryDrawResult>,
    lotteryFilter: String? = null,
    selectedYearA: Int? = null,
    selectedYearB: Int? = null,
  ): TemporalAnalysis {
    val filtered = filterByLottery(draws, lotteryFilter)
    if (filtered.isEmpty()) {
      return TemporalAnalysis(
        yearlyEvolution = emptyList(),
        monthlyDistribution = emptyList(),
        comparison = TemporalComparison(null, null, List(10) { 0.0 }, List(10) { 0.0 }, 0, 0),
      )
    }

    val byYear = filtered.filter { it.a_o_del_sorteo != null }.groupBy { it.a_o_del_sorteo!! }
    val yearlyPoints = byYear.entries.sortedBy { it.key }.map { (year, list) ->
      val numericTickets = list.mapNotNull { it.numericTicketValue }
      val mean = if (numericTickets.isNotEmpty()) numericTickets.average() else 0.0
      val digitCounts = IntArray(10)
      var totalD = 0
      for (d in list) {
        for (ch in d.fourDigitTicket) {
          val digit = ch.digitToIntOrNull() ?: continue
          digitCounts[digit]++
          totalD++
        }
      }
      var topD = 0
      var topCount = -1
      for (i in 0..9) {
        if (digitCounts[i] > topCount) {
          topCount = digitCounts[i]
          topD = i
        }
      }
      val topPct = if (totalD > 0) (topCount.toDouble() / totalD) * 100.0 else 0.0
      YearTemporalPoint(
        year = year,
        drawCount = list.size,
        meanTicketValue = mean,
        topDigit = topD,
        topDigitPct = topPct,
      )
    }

    val byMonth = filtered.filter { it.mes_del_sorteo in 1..12 }.groupBy { it.mes_del_sorteo!! }
    val monthlyPoints = (1..12).mapNotNull { m ->
      val list = byMonth[m] ?: return@mapNotNull null
      val nums = list.mapNotNull { it.numericTicketValue }
      MonthTemporalPoint(
        month = m,
        monthLabel = SPANISH_MONTHS[m - 1],
        drawCount = list.size,
        meanTicketValue = if (nums.isNotEmpty()) nums.average() else 0.0,
      )
    }

    val availableYearsDesc = byYear.keys.sortedDescending()
    val yearA = selectedYearA ?: availableYearsDesc.getOrNull(0)
    val yearB = selectedYearB ?: availableYearsDesc.getOrNull(1) ?: yearA

    val listA = yearA?.let { byYear[it] }.orEmpty()
    val listB = yearB?.let { byYear[it] }.orEmpty()

    return TemporalAnalysis(
      yearlyEvolution = yearlyPoints,
      monthlyDistribution = monthlyPoints,
      comparison = TemporalComparison(
        yearA = yearA,
        yearB = yearB,
        yearADigitPcts = computeDigitDistributionPcts(listA),
        yearBDigitPcts = computeDigitDistributionPcts(listB),
        yearADrawCount = listA.size,
        yearBDrawCount = listB.size,
      ),
    )
  }

  private fun computeDigitDistributionPcts(draws: List<LotteryDrawResult>): List<Double> {
    if (draws.isEmpty()) return List(10) { 0.0 }
    val counts = IntArray(10)
    var total = 0
    for (draw in draws) {
      for (ch in draw.fourDigitTicket) {
        val d = ch.digitToIntOrNull() ?: continue
        counts[d]++
        total++
      }
    }
    if (total == 0) return List(10) { 0.0 }
    return (0..9).map { d -> (counts[d].toDouble() / total) * 100.0 }
  }

  private fun filterByLottery(
    draws: List<LotteryDrawResult>,
    lotteryFilter: String?,
  ): List<LotteryDrawResult> {
    if (lotteryFilter.isNullOrBlank() || lotteryFilter == "Todas") return draws
    return draws.filter { it.loter_a.equals(lotteryFilter, ignoreCase = true) }
  }

  private fun extractValuesForTarget(
    draws: List<LotteryDrawResult>,
    target: AnalysisTarget,
  ): List<Double> {
    return when (target) {
      AnalysisTarget.WHOLE_NUMBER -> draws.mapNotNull { it.numericTicketValue?.toDouble() }
      AnalysisTarget.ALL_DIGITS -> draws.flatMap { draw ->
        draw.fourDigitTicket.mapNotNull { it.digitToIntOrNull()?.toDouble() }
      }
      AnalysisTarget.DIGIT_1 -> extractPositionalDigit(draws, 0)
      AnalysisTarget.DIGIT_2 -> extractPositionalDigit(draws, 1)
      AnalysisTarget.DIGIT_3 -> extractPositionalDigit(draws, 2)
      AnalysisTarget.DIGIT_4 -> extractPositionalDigit(draws, 3)
    }
  }

  private fun extractPositionalDigit(draws: List<LotteryDrawResult>, position: Int): List<Double> {
    return draws.mapNotNull { draw ->
      val ticket = draw.fourDigitTicket
      if (ticket.length == 4) ticket[position].digitToIntOrNull()?.toDouble() else null
    }
  }

  private fun computeDescriptiveStats(
    values: List<Double>,
    target: AnalysisTarget,
  ): DescriptiveStats {
    val n = values.size
    val sorted = values.sorted()
    val mean = values.average()
    val median = if (n % 2 == 1) {
      sorted[n / 2]
    } else {
      (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
    }

    val min = sorted.first()
    val max = sorted.last()
    val range = max - min

    // Varianza poblacional/muestral (N-1 si N>1 para análisis estadístico estándar)
    val sumSqDiff = values.sumOf { (it - mean).pow(2) }
    val variance = if (n > 1) sumSqDiff / (n - 1) else 0.0
    val stdDev = sqrt(variance)

    val countsMap = HashMap<Int, Int>()
    for (v in values) {
      val intVal = v.toInt()
      countsMap[intVal] = (countsMap[intVal] ?: 0) + 1
    }
    val maxFreq = countsMap.values.maxOrNull() ?: 0
    val modes = countsMap.entries
      .filter { it.value == maxFreq && maxFreq > 0 }
      .sortedBy { it.key }
      .take(4)
      .map { entry ->
        if (target.isDiscreteDigit) entry.key.toString()
        else entry.key.toString().padStart(4, '0')
      }

    val freqItems = if (target.isDiscreteDigit) {
      (0..9).map { d ->
        val c = countsMap[d] ?: 0
        val rel = c.toDouble() / n
        FrequencyItem(d.toString(), c, rel, rel * 100.0)
      }
    } else {
      countsMap.entries
        .sortedByDescending { it.value }
        .take(15)
        .map { (num, c) ->
          val rel = c.toDouble() / n
          FrequencyItem(num.toString().padStart(4, '0'), c, rel, rel * 100.0)
        }
    }

    return DescriptiveStats(
      sampleSize = n,
      mean = mean,
      median = median,
      modes = modes,
      modeFrequency = maxFreq,
      min = min,
      max = max,
      range = range,
      variance = variance,
      stdDev = stdDev,
      frequencyItems = freqItems,
    )
  }

  private fun buildDigitHistogramBins(
    values: List<Double>,
    mean: Double,
    stdDev: Double,
  ): List<HistogramBin> {
    val n = values.size
    val counts = IntArray(10)
    for (v in values) {
      val d = v.toInt().coerceIn(0, 9)
      counts[d]++
    }
    return (0..9).map { d ->
      val obsCount = counts[d]
      val obsPct = if (n > 0) (obsCount.toDouble() / n) * 100.0 else 0.0
      val theoCount = if (stdDev > 0.0) {
        normalPdf(d.toDouble(), mean, stdDev) * 1.0 * n
      } else 0.0
      val theoPct = if (n > 0) (theoCount / n) * 100.0 else 0.0
      HistogramBin(
        label = d.toString(),
        rangeStart = d.toDouble() - 0.5,
        rangeEnd = d.toDouble() + 0.5,
        midpoint = d.toDouble(),
        observedCount = obsCount,
        observedPct = obsPct,
        theoreticalNormalCount = theoCount,
        theoreticalNormalPct = theoPct,
      )
    }
  }

  private fun buildWholeNumberHistogramBins(
    values: List<Double>,
    mean: Double,
    stdDev: Double,
    binCount: Int = 10,
  ): List<HistogramBin> {
    val n = values.size
    val minRange = 0.0
    val maxRange = 10000.0
    val binWidth = (maxRange - minRange) / binCount
    val counts = IntArray(binCount)

    for (v in values) {
      val idx = ((v - minRange) / binWidth).toInt().coerceIn(0, binCount - 1)
      counts[idx]++
    }

    return (0 until binCount).map { i ->
      val start = minRange + i * binWidth
      val end = start + binWidth
      val mid = (start + end) / 2.0
      val obsCount = counts[i]
      val obsPct = if (n > 0) (obsCount.toDouble() / n) * 100.0 else 0.0
      val theoCount = if (stdDev > 0.0) {
        normalPdf(mid, mean, stdDev) * binWidth * n
      } else 0.0
      val theoPct = if (n > 0) (theoCount / n) * 100.0 else 0.0
      val label = "${(start.toInt() / 1000)}k-${(end.toInt() / 1000)}k"
      HistogramBin(
        label = label,
        rangeStart = start,
        rangeEnd = end,
        midpoint = mid,
        observedCount = obsCount,
        observedPct = obsPct,
        theoreticalNormalCount = theoCount,
        theoreticalNormalPct = theoPct,
      )
    }
  }

  private fun computeSigmaCoverage(
    values: List<Double>,
    mean: Double,
    stdDev: Double,
  ): Triple<Double, Double, Double> {
    if (values.isEmpty() || stdDev <= 0.0) return Triple(0.0, 0.0, 0.0)
    var c1 = 0
    var c2 = 0
    var c3 = 0
    for (v in values) {
      val z = abs(v - mean) / stdDev
      if (z <= 1.0) c1++
      if (z <= 2.0) c2++
      if (z <= 3.0) c3++
    }
    val n = values.size.toDouble()
    return Triple((c1 / n) * 100.0, (c2 / n) * 100.0, (c3 / n) * 100.0)
  }

  private fun computeSkewness(values: List<Double>, mean: Double, stdDev: Double): Double {
    if (values.size < 3 || stdDev <= 0.0) return 0.0
    val n = values.size.toDouble()
    val m3 = values.sumOf { ((it - mean) / stdDev).pow(3) }
    return m3 / n
  }

  private fun evaluateNormalSuitability(
    target: AnalysisTarget,
    sampleSize: Int,
    stdDev: Double,
    skewness: Double,
  ): Triple<Boolean, String, String> {
    if (sampleSize < 30) {
      return Triple(
        false,
        "Muestra reducida (n = $sampleSize)",
        "Se requieren al menos 30 registros para evaluar una aproximación distribucional descriptiva confiable.",
      )
    }
    if (stdDev <= 0.0) {
      return Triple(
        false,
        "Varianza nula",
        "Todos los valores observados son idénticos; no existe dispersión para ajustar una curva normal.",
      )
    }
    if (target.isDiscreteDigit) {
      return Triple(
        false,
        "Variable discreta (dígitos 0–9): la curva normal no aplica",
        "Los dígitos individuales (0–9) son categorías discretas acotadas cuya referencia teórica en sorteos independientes es una distribución uniforme (10% por dígito), no una campana de Gauss. Se muestra la línea de referencia uniforme del 10% para comparar la frecuencia observada.",
      )
    }
    return Triple(
      true,
      "Comparación descriptiva frente a curva normal",
      "Se superpone la campana normal teórica (μ, σ) únicamente como referencia descriptiva sobre los intervalos 0000–9999 (asimetría: ${"%.2f".format(skewness)}). En sorteos justos, el número completo tiende a una distribución uniforme discreta más plana que una normal, por lo que no debe interpretarse como predictor.",
    )
  }

  private fun normalPdf(x: Double, mean: Double, stdDev: Double): Double {
    if (stdDev <= 0.0) return 0.0
    val exponent = -0.5 * ((x - mean) / stdDev).pow(2)
    return (1.0 / (stdDev * sqrt(2.0 * PI))) * exp(exponent)
  }

  private fun mapToTopFrequencyItems(
    counts: Map<String, Int>,
    denominator: Int,
    limit: Int,
  ): List<FrequencyItem> {
    val denom = denominator.coerceAtLeast(1)
    return counts.entries
      .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
      .take(limit)
      .map { (key, count) ->
        val rel = count.toDouble() / denom
        FrequencyItem(
          label = key,
          count = count,
          relativeFrequency = rel,
          percentage = rel * 100.0,
        )
      }
  }
}
