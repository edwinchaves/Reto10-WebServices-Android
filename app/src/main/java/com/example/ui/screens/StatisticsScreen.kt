package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.statistics.AnalysisTarget
import com.example.domain.statistics.BellCurveAnalysis
import com.example.domain.statistics.FrequencyItem
import com.example.domain.statistics.FrequencySummary
import com.example.domain.statistics.TemporalAnalysis
import com.example.ui.components.DigitFrequencyBarChart
import com.example.ui.components.EmptyDataStateCard
import com.example.ui.components.HistogramBellCurveChart
import com.example.ui.components.StatisticalDisclaimerBanner
import com.example.ui.components.YearComparisonChart
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.SurfaceNavyHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurquoiseBright
import com.example.ui.theme.VioletLight
import com.example.ui.viewmodel.StatisticsSection
import com.example.ui.viewmodel.StatisticsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
  uiState: StatisticsUiState,
  onSelectSection: (StatisticsSection) -> Unit,
  onSelectTarget: (AnalysisTarget) -> Unit,
  onSelectLottery: (String) -> Unit,
  onSelectComparisonYears: (Int?, Int?) -> Unit,
  onNavigateToSync: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 840.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Análisis Estadístico",
          style = MaterialTheme.typography.headlineSmall,
          color = TextPrimary,
        )

        SimpleDropdownSelector(
          label = "Lotería analizada",
          selectedLabel = uiState.selectedLottery,
          options = uiState.availableLotteries,
          onSelect = onSelectLottery,
          testTag = "stats_lottery_dropdown",
        )

        PrimaryTabRow(
          selectedTabIndex = uiState.activeSection.ordinal,
          containerColor = SurfaceNavy,
          contentColor = TurquoiseBright,
        ) {
          StatisticsSection.entries.forEach { section ->
            Tab(
              selected = uiState.activeSection == section,
              onClick = { onSelectSection(section) },
              modifier = Modifier.testTag("stats_tab_${section.name.lowercase()}"),
              text = {
                Text(
                  text = section.label,
                  style = MaterialTheme.typography.labelLarge,
                  maxLines = 1,
                )
              },
            )
          }
        }
      }

      if (uiState.totalDrawsAvailable == 0) {
        Box(modifier = Modifier.padding(16.dp)) {
          EmptyDataStateCard(
            title = "Sin registros históricos para analizar",
            subtitle = "Sincroniza los datos desde Inicio para calcular estadísticas descriptivas, curvas y frecuencias.",
            actionLabel = "Ir a Inicio",
            onAction = onNavigateToSync,
          )
        }
      } else {
        when (uiState.activeSection) {
          StatisticsSection.DESCRIPTIVE_BELL -> {
            DescriptiveAndBellSection(
              selectedTarget = uiState.selectedTarget,
              analysis = uiState.bellCurveAnalysis,
              onSelectTarget = onSelectTarget,
            )
          }
          StatisticsSection.FREQUENCIES -> {
            uiState.frequencySummary?.let { freq ->
              FrequenciesSection(summary = freq)
            }
          }
          StatisticsSection.TEMPORAL -> {
            uiState.temporalAnalysis?.let { temporal ->
              TemporalSection(
                temporal = temporal,
                availableYears = uiState.availableYears,
                selectedYearA = uiState.selectedYearA,
                selectedYearB = uiState.selectedYearB,
                onSelectComparisonYears = onSelectComparisonYears,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DescriptiveAndBellSection(
  selectedTarget: AnalysisTarget,
  analysis: BellCurveAnalysis?,
  onSelectTarget: (AnalysisTarget) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Variable de análisis (sin mezclar escalas)",
          style = MaterialTheme.typography.labelLarge,
          color = TextSecondary,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AnalysisTarget.entries.forEach { target ->
            val selected = target == selectedTarget
            FilterChip(
              selected = selected,
              onClick = { onSelectTarget(target) },
              modifier = Modifier.testTag("target_chip_${target.name.lowercase()}"),
              label = { Text(target.label) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ElectricBlueContainer,
                selectedLabelColor = TurquoiseBright,
              ),
            )
          }
        }
      }
    }

    if (analysis != null) {
      item {
        StatisticalDisclaimerBanner(
          title = analysis.statisticalNoticeTitle,
          body = analysis.statisticalNoticeBody,
          isWarningStyle = !analysis.isNormalFitAppropriate,
        )
      }

      item {
        HistogramBellCurveChart(analysis = analysis)
      }

      // Descriptive stats grid
      item {
        val s = analysis.stats
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "Estadísticas Descriptivas",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
            )
            StatRowPair("Media aritmética (μ)", formatNum(s.mean, analysis.target), "Mediana", formatNum(s.median, analysis.target))
            StatRowPair(
              "Moda(s)",
              s.modes.joinToString(", ").ifEmpty { "-" } + " (${s.modeFrequency}x)",
              "Rango",
              formatNum(s.range, analysis.target),
            )
            StatRowPair("Mínimo", formatNum(s.min, analysis.target), "Máximo", formatNum(s.max, analysis.target))
            StatRowPair("Varianza (s²)", "%.2f".format(s.variance), "Desv. estándar (σ)", "%.2f".format(s.stdDev))
          }
        }
      }

      // Sigma intervals comparison
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Intervalos de Desviación Estándar (Observado vs Normal Teórica)",
              style = MaterialTheme.typography.titleSmall,
              color = TextPrimary,
            )
            SigmaComparisonRow("μ ± 1σ", analysis.withinOneSigmaObservedPct, 68.27)
            SigmaComparisonRow("μ ± 2σ", analysis.withinTwoSigmaObservedPct, 95.45)
            SigmaComparisonRow("μ ± 3σ", analysis.withinThreeSigmaObservedPct, 99.73)
          }
        }
      }

      // Absolute, Relative & Percentage distribution table
      item {
        FrequencyTableCard(
          title = if (analysis.target.isDiscreteDigit) {
            "Distribución de Frecuencias (Dígitos 0–9)"
          } else {
            "Números Ganadores Más Frecuentes"
          },
          items = analysis.stats.frequencyItems,
        )
      }
    }
  }
}

@Composable
private fun FrequenciesSection(summary: FrequencySummary) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      DigitFrequencyBarChart(
        title = "Frecuencia global por dígito (n = ${summary.totalDigitsAnalyzed} dígitos)",
        items = summary.positionalMatrix.map {
          FrequencyItem(it.digit.toString(), it.totalCount, it.totalPct / 100.0, it.totalPct)
        },
      )
    }

    // Positional Matrix
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
        border = BorderStroke(1.dp, BorderSubtle),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Frecuencia por Posición (Billete 4 cifras)",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            MatrixHeaderCell("Díg", Modifier.weight(0.7f))
            MatrixHeaderCell("Pos 1", Modifier.weight(1f))
            MatrixHeaderCell("Pos 2", Modifier.weight(1f))
            MatrixHeaderCell("Pos 3", Modifier.weight(1f))
            MatrixHeaderCell("Pos 4", Modifier.weight(1f))
            MatrixHeaderCell("Total %", Modifier.weight(1.1f))
          }
          HorizontalDivider(color = BorderSubtle)
          summary.positionalMatrix.forEach { row ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = row.digit.toString(),
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                color = TurquoiseBright,
                modifier = Modifier.weight(0.7f),
                textAlign = TextAlign.Center,
              )
              MatrixValueCell(row.pos1Count.toString(), Modifier.weight(1f))
              MatrixValueCell(row.pos2Count.toString(), Modifier.weight(1f))
              MatrixValueCell(row.pos3Count.toString(), Modifier.weight(1f))
              MatrixValueCell(row.pos4Count.toString(), Modifier.weight(1f))
              Text(
                text = "${"%.1f".format(row.totalPct)}%",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                color = ElectricBlueLight,
                modifier = Modifier.weight(1.1f),
                textAlign = TextAlign.End,
              )
            }
          }
        }
      }
    }

    item {
      FrequencyTableCard(
        title = "Combinaciones de 2 cifras más repetidas",
        items = summary.topTwoDigitCombinations,
      )
    }

    item {
      FrequencyTableCard(
        title = "Combinaciones de 3 cifras más repetidas",
        items = summary.topThreeDigitCombinations,
      )
    }

    item {
      FrequencyTableCard(
        title = "Terminaciones más frecuentes (últimas 2 cifras)",
        items = summary.topLastTwoDigitEndings,
      )
    }

    item {
      FrequencyTableCard(
        title = "Terminaciones de 3 cifras más frecuentes",
        items = summary.topLastThreeDigitEndings,
      )
    }

    item {
      FrequencyTableCard(
        title = "Repeticiones históricas de billete completo",
        items = summary.repeatedFullTickets,
      )
    }

    item {
      FrequencyTableCard(
        title = "Registros por Lotería",
        items = summary.drawsByLottery,
      )
    }
  }
}

@Composable
private fun TemporalSection(
  temporal: TemporalAnalysis,
  availableYears: List<Int>,
  selectedYearA: Int?,
  selectedYearB: Int?,
  onSelectComparisonYears: (Int?, Int?) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      StatisticalDisclaimerBanner(
        title = "Interpretación temporal",
        body = "Observar variaciones por año o mes permite describir cambios muestrales históricos, pero no implica tendencias ni aumenta la probabilidad futura de ningún número.",
        isWarningStyle = false,
      )
    }

    if (availableYears.isNotEmpty()) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          SimpleDropdownSelector(
            label = "Año A",
            selectedLabel = selectedYearA?.toString() ?: "-",
            options = availableYears.map { it.toString() },
            onSelect = { chosen ->
              onSelectComparisonYears(chosen.toIntOrNull(), selectedYearB)
            },
            modifier = Modifier.weight(1f),
            testTag = "temporal_year_a_dropdown",
          )
          SimpleDropdownSelector(
            label = "Año B",
            selectedLabel = selectedYearB?.toString() ?: "-",
            options = availableYears.map { it.toString() },
            onSelect = { chosen ->
              onSelectComparisonYears(selectedYearA, chosen.toIntOrNull())
            },
            modifier = Modifier.weight(1f),
            testTag = "temporal_year_b_dropdown",
          )
        }
      }

      item {
        YearComparisonChart(
          yearA = temporal.comparison.yearA,
          yearB = temporal.comparison.yearB,
          yearAPcts = temporal.comparison.yearADigitPcts,
          yearBPcts = temporal.comparison.yearBDigitPcts,
        )
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
        border = BorderStroke(1.dp, BorderSubtle),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Evolución Anual de Sorteos",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
          )
          temporal.yearlyEvolution.sortedByDescending { it.year }.forEach { pt ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = pt.year.toString(),
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                color = TurquoiseBright,
              )
              Text(
                text = "${pt.drawCount} sorteos",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
              )
              Text(
                text = "Media: ${"%.0f".format(pt.meanTicketValue)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                color = ElectricBlueLight,
              )
              Text(
                text = "Díg. top: ${pt.topDigit} (${"%.1f".format(pt.topDigitPct)}%)",
                style = MaterialTheme.typography.labelSmall,
                color = VioletLight,
              )
            }
            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
          }
        }
      }
    }

    if (temporal.monthlyDistribution.isNotEmpty()) {
      item {
        DigitFrequencyBarChart(
          title = "Comportamiento Mensual (Cantidad de sorteos)",
          items = temporal.monthlyDistribution.map { m ->
            val total = temporal.monthlyDistribution.sumOf { it.drawCount }.coerceAtLeast(1)
            val rel = m.drawCount.toDouble() / total
            FrequencyItem(m.monthLabel, m.drawCount, rel, rel * 100.0)
          },
        )
      }
    }
  }
}

@Composable
private fun FrequencyTableCard(
  title: String,
  items: List<FrequencyItem>,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
      )
      if (items.isEmpty()) {
        Text("Sin datos suficientes", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text("Valor", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1.4f))
          Text("Frec. Abs.", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
          Text("Relativa", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
          Text("%", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
        }
        HorizontalDivider(color = BorderSubtle)
        items.forEach { item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = item.label,
              fontFamily = JetBrainsMonoFamily,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary,
              modifier = Modifier.weight(1.4f),
            )
            Text(
              text = item.count.toString(),
              fontFamily = JetBrainsMonoFamily,
              fontSize = 12.sp,
              color = ElectricBlueLight,
              modifier = Modifier.weight(0.8f),
              textAlign = TextAlign.End,
            )
            Text(
              text = "%.3f".format(item.relativeFrequency),
              fontFamily = JetBrainsMonoFamily,
              fontSize = 12.sp,
              color = TextSecondary,
              modifier = Modifier.weight(0.8f),
              textAlign = TextAlign.End,
            )
            Text(
              text = "${"%.1f".format(item.percentage)}%",
              fontFamily = JetBrainsMonoFamily,
              fontSize = 12.sp,
              color = TurquoiseBright,
              modifier = Modifier.weight(0.8f),
              textAlign = TextAlign.End,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatRowPair(
  label1: String,
  val1: String,
  label2: String,
  val2: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Surface(
      modifier = Modifier.weight(1f),
      color = SurfaceNavyHighlight,
      shape = RoundedCornerShape(12.dp),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Text(label1, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(
          text = val1,
          fontFamily = JetBrainsMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = TurquoiseBright,
        )
      }
    }
    Surface(
      modifier = Modifier.weight(1f),
      color = SurfaceNavyHighlight,
      shape = RoundedCornerShape(12.dp),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Text(label2, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(
          text = val2,
          fontFamily = JetBrainsMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = ElectricBlueLight,
        )
      }
    }
  }
}

@Composable
private fun SigmaComparisonRow(
  interval: String,
  observedPct: Double,
  theoreticalPct: Double,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = interval,
      fontFamily = JetBrainsMonoFamily,
      fontWeight = FontWeight.Bold,
      color = VioletLight,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = "Obs: ${"%.1f".format(observedPct)}%",
      fontFamily = JetBrainsMonoFamily,
      fontSize = 13.sp,
      color = ElectricBlueLight,
      modifier = Modifier.weight(1.2f),
    )
    Text(
      text = "Normal teór: ${"%.1f".format(theoreticalPct)}%",
      fontFamily = JetBrainsMonoFamily,
      fontSize = 12.sp,
      color = TextSecondary,
      modifier = Modifier.weight(1.5f),
      textAlign = TextAlign.End,
    )
  }
}

@Composable
private fun MatrixHeaderCell(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    color = TextSecondary,
    modifier = modifier,
    textAlign = TextAlign.Center,
  )
}

@Composable
private fun MatrixValueCell(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    fontFamily = JetBrainsMonoFamily,
    fontSize = 12.sp,
    color = TextPrimary,
    modifier = modifier,
    textAlign = TextAlign.Center,
  )
}

private fun formatNum(value: Double, target: AnalysisTarget): String {
  return if (target.isDiscreteDigit) "%.2f".format(value) else "%.1f".format(value)
}
