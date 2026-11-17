package com.example.domain.generator

import com.example.data.model.LotteryDrawResult
import java.security.SecureRandom
import java.util.UUID
import kotlin.math.max

object ChanceGeneratorEngine {

  private val secureRandom = SecureRandom()

  fun generateCombinations(
    draws: List<LotteryDrawResult>,
    modality: ChanceModality,
    strategy: GenerationStrategy,
    referenceLottery: String,
    window: HistoricalWindow,
    quantity: Int,
  ): List<GeneratedCombination> {
    val count = quantity.coerceIn(1, 10)
    val lotteryFiltered = if (referenceLottery == "Todas" || referenceLottery.isBlank()) {
      draws
    } else {
      draws.filter { it.loter_a.equals(referenceLottery, ignoreCase = true) }
    }

    val windowedDraws = applyHistoricalWindow(lotteryFiltered, window)
      .ifEmpty { lotteryFiltered }
      .sortedByDescending { it.formattedDate }

    val digitsCount = modality.digitsCount
    val startPosOffset = if (digitsCount == 3) 1 else 0

    // Matriz de frecuencias posicionales (4 posiciones x 10 dígitos)
    val posCounts = Array(4) { IntArray(10) }
    val weightedPosScores = Array(4) { DoubleArray(10) }
    val totalSamples = windowedDraws.size

    windowedDraws.forEachIndexed { index, draw ->
      val ticket = draw.fourDigitTicket
      if (ticket.length == 4) {
        // Para estrategia reciente, los primeros índices (más recientes) reciben mayor peso
        val recencyWeight = if (totalSamples > 1) {
          1.0 + 2.5 * (1.0 - index.toDouble() / (totalSamples - 1).toDouble())
        } else {
          1.0
        }
        for (p in 0 until 4) {
          val d = ticket[p].digitToIntOrNull() ?: continue
          posCounts[p][d]++
          weightedPosScores[p][d] += recencyWeight
        }
      }
    }

    // Calcular niveles (Alta / Media / Baja frecuencia) por posición
    val posTiers = Array(4) { p -> computeDigitTiers(posCounts[p]) }

    val generated = ArrayList<GeneratedCombination>(count)
    val usedNumbers = HashSet<String>()
    var attempts = 0

    while (generated.size < count && attempts < count * 25) {
      attempts++
      val digits = IntArray(digitsCount) { i ->
        val pos = i + startPosOffset
        when (strategy) {
          GenerationStrategy.HISTORICAL_FREQUENCY -> {
            val weights = DoubleArray(10) { d -> posCounts[pos][d].toDouble() + 1.0 }
            weightedPick(weights)
          }
          GenerationStrategy.RECENT_FREQUENCY -> {
            val weights = DoubleArray(10) { d -> weightedPosScores[pos][d] + 1.0 }
            weightedPick(weights)
          }
          GenerationStrategy.MIXED -> {
            pickMixedDigitForSlot(
              slotIndex = i,
              digitsCount = digitsCount,
              tiers = posTiers[pos],
              counts = posCounts[pos],
            )
          }
          GenerationStrategy.RANDOM -> {
            secureRandom.nextInt(10)
          }
        }
      }

      val numberStr = digits.joinToString("")
      if (!usedNumbers.add(numberStr) && attempts < count * 15) {
        continue
      }

      val digitStats = digits.mapIndexed { idx, d ->
        val pos = idx + startPosOffset
        val occ = posCounts[pos][d]
        val pct = if (totalSamples > 0) (occ.toDouble() / totalSamples) * 100.0 else 0.0
        DigitPositionStat(
          positionIndex = idx + 1,
          digit = d,
          positionalOccurrences = occ,
          positionalPct = pct,
          tierLabel = posTiers[pos][d],
        )
      }

      val exactMatches = windowedDraws.count { draw ->
        if (digitsCount == 4) draw.fourDigitTicket == numberStr
        else draw.threeDigitChance == numberStr
      }
      val lastTwo = numberStr.takeLast(2)
      val endingTwoMatches = windowedDraws.count { it.twoDigitEnding == lastTwo }

      val justification = buildJustification(
        strategy = strategy,
        numberStr = numberStr,
        modality = modality,
        referenceLottery = referenceLottery,
        window = window,
        sampleSize = totalSamples,
        digitStats = digitStats,
        exactMatches = exactMatches,
      )

      generated.add(
        GeneratedCombination(
          id = UUID.randomUUID().toString(),
          number = numberStr,
          modality = modality,
          strategy = strategy,
          referenceLottery = referenceLottery,
          window = window,
          sampleSizeUsed = totalSamples,
          digitStats = digitStats,
          exactHistoricalMatches = exactMatches,
          endingTwoDigitMatches = endingTwoMatches,
          justification = justification,
        )
      )
    }

    return generated
  }

  private fun applyHistoricalWindow(
    draws: List<LotteryDrawResult>,
    window: HistoricalWindow,
  ): List<LotteryDrawResult> {
    val monthsBack = window.monthsBack ?: return draws
    if (draws.isEmpty()) return draws

    // Tomar como referencia la fecha más reciente disponible en los datos descargados
    val latestSortKey = draws.maxOfOrNull { drawSortKey(it) } ?: return draws
    val latestYear = latestSortKey / 100
    val latestMonth = latestSortKey % 100
    val totalLatestMonths = latestYear * 12 + latestMonth
    val cutoffMonths = totalLatestMonths - monthsBack

    return draws.filter { draw ->
      val key = drawSortKey(draw)
      val y = key / 100
      val m = key % 100
      (y * 12 + m) >= cutoffMonths
    }
  }

  private fun drawSortKey(draw: LotteryDrawResult): Int {
    val y = draw.a_o_del_sorteo ?: draw.formattedDate.take(4).toIntOrNull() ?: 0
    val m = draw.mes_del_sorteo ?: draw.formattedDate.drop(5).take(2).toIntOrNull() ?: 1
    return y * 100 + m.coerceIn(1, 12)
  }

  private fun computeDigitTiers(counts: IntArray): Array<String> {
    val indexed = (0..9).sortedByDescending { counts[it] }
    val tiers = Array(10) { "Media" }
    indexed.take(3).forEach { tiers[it] = "Alta" }
    indexed.takeLast(3).forEach { tiers[it] = "Baja" }
    return tiers
  }

  private fun pickMixedDigitForSlot(
    slotIndex: Int,
    digitsCount: Int,
    tiers: Array<String>,
    counts: IntArray,
  ): Int {
    val desiredTier = when {
      digitsCount == 3 -> when (slotIndex) {
        0 -> "Alta"
        1 -> "Media"
        else -> "Baja"
      }
      else -> when (slotIndex) {
        0, 2 -> "Alta"
        1 -> "Media"
        else -> "Baja"
      }
    }
    val candidates = (0..9).filter { tiers[it] == desiredTier }
    if (candidates.isEmpty()) return secureRandom.nextInt(10)
    // Selección aleatoria entre candidatos del grupo (o ligeramente ponderada si hay datos)
    val total = candidates.sumOf { max(1, counts[it]) }
    if (total <= 0) return candidates[secureRandom.nextInt(candidates.size)]
    return candidates[secureRandom.nextInt(candidates.size)]
  }

  private fun weightedPick(weights: DoubleArray): Int {
    val total = weights.sum()
    if (total <= 0.0) return secureRandom.nextInt(10)
    var r = secureRandom.nextDouble() * total
    for (i in weights.indices) {
      r -= weights[i]
      if (r <= 0.0) return i
    }
    return weights.lastIndex
  }

  private fun buildJustification(
    strategy: GenerationStrategy,
    numberStr: String,
    modality: ChanceModality,
    referenceLottery: String,
    window: HistoricalWindow,
    sampleSize: Int,
    digitStats: List<DigitPositionStat>,
    exactMatches: Int,
  ): String {
    val avgPct = if (digitStats.isNotEmpty()) digitStats.map { it.positionalPct }.average() else 0.0
    val tiersSummary = digitStats.joinToString(" · ") { "P${it.positionIndex}:${it.digit}(${it.tierLabel})" }
    val scope = if (referenceLottery == "Todas") "todas las loterías" else referenceLottery

    return when (strategy) {
      GenerationStrategy.HISTORICAL_FREQUENCY ->
        "Construida mediante muestreo ponderado por frecuencia histórica sobre $sampleSize sorteos ($scope, ${window.label}). Promedio de frecuencia posicional de sus dígitos: ${"%.1f".format(avgPct)}%. Composición: $tiersSummary."
      GenerationStrategy.RECENT_FREQUENCY ->
        "Generada priorizando descriptivamente los sorteos más recientes dentro de ${window.label} ($sampleSize registros de $scope). Perfil posicional: $tiersSummary."
      GenerationStrategy.MIXED ->
        "Combinación balanceada que integra dígitos de frecuencia histórica Alta, Media y Baja ($tiersSummary) sobre $sampleSize registros de $scope. Coincidencias exactas pasadas en ${modality.label}: $exactMatches."
      GenerationStrategy.RANDOM ->
        "Combinación $numberStr generada de forma uniforme e independiente por dígito (0–9, conservando ceros iniciales). En $sampleSize registros analizados ($scope) registra $exactMatches apariciones históricas exactas."
    }
  }
}
