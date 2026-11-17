package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.statistics.BellCurveAnalysis
import com.example.domain.statistics.FrequencyItem
import com.example.domain.statistics.HistogramBin
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SurfaceNavyElevated
import com.example.ui.theme.SurfaceNavyHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurquoiseBright
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun HistogramBellCurveChart(
  analysis: BellCurveAnalysis,
  modifier: Modifier = Modifier,
) {
  val bins = analysis.bins
  var selectedIndex by remember(analysis) { mutableIntStateOf(-1) }
  val textMeasurer = rememberTextMeasurer()

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Histograma y Distribución",
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimary,
        )
        Text(
          text = "n = ${analysis.stats.sampleSize}",
          fontFamily = JetBrainsMonoFamily,
          fontSize = 12.sp,
          color = TurquoiseBright,
        )
      }

      // Leyenda
      Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ChartLegendItem(color = ElectricBlue, label = "Frecuencia observada")
        ChartLegendItem(
          color = if (analysis.isNormalFitAppropriate) TurquoiseBright else AmberWarning,
          label = if (analysis.isNormalFitAppropriate) "Curva normal teórica" else "Referencia uniforme (10%)",
        )
      }

      // Tooltip al tocar una barra
      val selectedBin: HistogramBin? = bins.getOrNull(selectedIndex)
      if (selectedBin != null) {
        Surface(
          color = SurfaceNavyHighlight,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Intervalo: ${selectedBin.label}",
              style = MaterialTheme.typography.labelMedium,
              color = TextPrimary,
            )
            Text(
              text = "Obs: ${selectedBin.observedCount} (${"%.1f".format(selectedBin.observedPct)}%)",
              fontFamily = JetBrainsMonoFamily,
              fontSize = 12.sp,
              color = ElectricBlueLight,
            )
            if (analysis.isNormalFitAppropriate) {
              Text(
                text = "Teór: ${"%.0f".format(selectedBin.theoreticalNormalCount)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                color = TurquoiseBright,
              )
            }
          }
        }
      } else {
        Text(
          text = "Toca cualquier barra para inspeccionar frecuencias exactas",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
        )
      }

      if (bins.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text("Sin datos para graficar", color = TextSecondary)
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(bins) {
                detectTapGestures { offset ->
                  val leftPad = 36.dp.toPx()
                  val rightPad = 12.dp.toPx()
                  val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                  val relX = offset.x - leftPad
                  if (relX in 0f..chartW) {
                    val idx = ((relX / chartW) * bins.size).toInt().coerceIn(0, bins.lastIndex)
                    selectedIndex = if (selectedIndex == idx) -1 else idx
                  }
                }
              },
          ) {
            val leftPad = 36.dp.toPx()
            val rightPad = 12.dp.toPx()
            val topPad = 14.dp.toPx()
            val bottomPad = 30.dp.toPx()

            val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

            val maxObs = bins.maxOfOrNull { it.observedCount } ?: 1
            val maxTheo = if (analysis.isNormalFitAppropriate) {
              bins.maxOfOrNull { it.theoreticalNormalCount } ?: 0.0
            } else {
              analysis.stats.sampleSize * 0.10
            }
            val maxY = max(maxObs.toDouble(), maxTheo).coerceAtLeast(1.0) * 1.15

            // Líneas guía horizontales
            val gridSteps = 4
            for (step in 0..gridSteps) {
              val frac = step.toFloat() / gridSteps
              val y = topPad + chartH * (1f - frac)
              drawLine(
                color = BorderSubtle.copy(alpha = 0.6f),
                start = Offset(leftPad, y),
                end = Offset(leftPad + chartW, y),
                strokeWidth = 1.dp.toPx(),
              )
              val labelVal = ((maxY * frac)).toInt().toString()
              drawAxisText(
                textMeasurer = textMeasurer,
                text = labelVal,
                topLeft = Offset(2.dp.toPx(), y - 7.dp.toPx()),
                color = TextMuted,
              )
            }

            // Barras del histograma
            val slotW = chartW / bins.size
            val barW = slotW * 0.68f
            val barGap = (slotW - barW) / 2f

            bins.forEachIndexed { idx, bin ->
              val ratio = (bin.observedCount / maxY).toFloat().coerceIn(0f, 1f)
              val barH = chartH * ratio
              val x = leftPad + idx * slotW + barGap
              val y = topPad + chartH - barH

              val isSelected = idx == selectedIndex
              val barBrush = Brush.verticalGradient(
                colors = if (isSelected) {
                  listOf(TurquoiseBright, ElectricBlue)
                } else {
                  listOf(ElectricBlue, DeepViolet.copy(alpha = 0.85f))
                }
              )

              drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
              )

              // Etiqueta eje X
              if (bins.size <= 10 || idx % 2 == 0) {
                drawAxisText(
                  textMeasurer = textMeasurer,
                  text = bin.label,
                  topLeft = Offset(x + barW * 0.1f, topPad + chartH + 6.dp.toPx()),
                  color = if (isSelected) TurquoiseBright else TextSecondary,
                  fontSize = 9.sp,
                )
              }
            }

            // Curva normal o línea uniforme de referencia
            if (analysis.isNormalFitAppropriate && analysis.stats.stdDev > 0.0) {
              val minDomain = bins.first().rangeStart
              val maxDomain = bins.last().rangeEnd
              val binWidth = bins.first().rangeEnd - bins.first().rangeStart
              val mean = analysis.stats.mean
              val stdDev = analysis.stats.stdDev
              val n = analysis.stats.sampleSize

              // Marcador de media (μ) y ±1σ
              fun xForValue(v: Double): Float {
                val frac = ((v - minDomain) / (maxDomain - minDomain)).toFloat().coerceIn(0f, 1f)
                return leftPad + frac * chartW
              }

              val meanX = xForValue(mean)
              drawLine(
                color = AmberWarning.copy(alpha = 0.85f),
                start = Offset(meanX, topPad),
                end = Offset(meanX, topPad + chartH),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
              )

              listOf(mean - stdDev, mean + stdDev).forEach { sigmaVal ->
                if (sigmaVal in minDomain..maxDomain) {
                  val sx = xForValue(sigmaVal)
                  drawLine(
                    color = DeepViolet.copy(alpha = 0.7f),
                    start = Offset(sx, topPad + 8.dp.toPx()),
                    end = Offset(sx, topPad + chartH),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 7f)),
                  )
                }
              }

              // Curva suave muestreada en 60 puntos
              val curvePath = Path()
              val samples = 60
              for (s in 0..samples) {
                val frac = s.toFloat() / samples
                val domainVal = minDomain + frac * (maxDomain - minDomain)
                val pdf = (1.0 / (stdDev * sqrt(2.0 * PI))) *
                  exp(-0.5 * ((domainVal - mean) / stdDev).pow(2))
                val expectedCount = pdf * binWidth * n
                val px = leftPad + frac * chartW
                val py = topPad + chartH * (1f - (expectedCount / maxY).toFloat().coerceIn(0f, 1f))
                if (s == 0) curvePath.moveTo(px, py) else curvePath.lineTo(px, py)
              }

              drawPath(
                path = curvePath,
                color = TurquoiseBright,
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round),
              )
            } else if (!analysis.isNormalFitAppropriate && analysis.target.isDiscreteDigit) {
              // Línea de referencia uniforme (10% esperado para dígitos 0-9)
              val expectedUniform = analysis.stats.sampleSize * 0.10
              val uy = topPad + chartH * (1f - (expectedUniform / maxY).toFloat().coerceIn(0f, 1f))
              drawLine(
                color = AmberWarning,
                start = Offset(leftPad, uy),
                end = Offset(leftPad + chartW, uy),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun DigitFrequencyBarChart(
  title: String,
  items: List<FrequencyItem>,
  modifier: Modifier = Modifier,
) {
  var selectedIdx by remember(items) { mutableIntStateOf(-1) }
  val textMeasurer = rememberTextMeasurer()

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimary,
        )
        val picked = items.getOrNull(selectedIdx)
        if (picked != null) {
          Text(
            text = "Dígito ${picked.label}: ${picked.count} (${"%.1f".format(picked.percentage)}%)",
            fontFamily = JetBrainsMonoFamily,
            fontSize = 12.sp,
            color = TurquoiseBright,
          )
        }
      }

      if (items.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text("Sin datos disponibles", color = TextSecondary)
        }
      } else {
        val maxVal = items.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
        val minVal = items.minOfOrNull { it.count } ?: 0

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .pointerInput(items) {
              detectTapGestures { offset ->
                val pad = 8.dp.toPx()
                val usableW = (size.width - pad * 2).coerceAtLeast(1f)
                val idx = (((offset.x - pad) / usableW) * items.size).toInt().coerceIn(0, items.lastIndex)
                selectedIdx = if (selectedIdx == idx) -1 else idx
              }
            },
        ) {
          val padH = 8.dp.toPx()
          val topPad = 18.dp.toPx()
          val bottomPad = 24.dp.toPx()
          val usableW = (size.width - padH * 2).coerceAtLeast(1f)
          val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)
          val slotW = usableW / items.size
          val barW = slotW * 0.62f

          items.forEachIndexed { i, item ->
            val ratio = (item.count.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)
            val barH = chartH * ratio
            val x = padH + i * slotW + (slotW - barW) / 2f
            val y = topPad + chartH - barH

            val barColor = when {
              i == selectedIdx -> AmberWarning
              item.count == maxVal -> TurquoiseBright
              item.count == minVal -> DeepViolet
              else -> ElectricBlue
            }

            drawRoundRect(
              color = barColor,
              topLeft = Offset(x, y),
              size = Size(barW, barH),
              cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )

            drawAxisText(
              textMeasurer = textMeasurer,
              text = "${"%.0f".format(item.percentage)}%",
              topLeft = Offset(x, y - 14.dp.toPx()),
              color = TextSecondary,
              fontSize = 9.sp,
            )

            drawAxisText(
              textMeasurer = textMeasurer,
              text = item.label,
              topLeft = Offset(x + barW * 0.3f, topPad + chartH + 5.dp.toPx()),
              color = TextPrimary,
              fontSize = 11.sp,
            )
          }
        }
      }
    }
  }
}

@Composable
fun YearComparisonChart(
  yearA: Int?,
  yearB: Int?,
  yearAPcts: List<Double>,
  yearBPcts: List<Double>,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceNavyElevated),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = "Comparación de Distribución por Dígito (0–9)",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ChartLegendItem(color = ElectricBlue, label = "Año ${yearA ?: "-"}")
        ChartLegendItem(color = TurquoiseBright, label = "Año ${yearB ?: "-"}")
      }

      if (yearAPcts.size != 10 || yearBPcts.size != 10) {
        Text("Sin datos comparativos suficientes", color = TextSecondary)
      } else {
        val maxPct = max(
          yearAPcts.maxOrNull() ?: 15.0,
          yearBPcts.maxOrNull() ?: 15.0,
        ).coerceAtLeast(12.0) * 1.15

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        ) {
          val leftPad = 32.dp.toPx()
          val topPad = 12.dp.toPx()
          val bottomPad = 24.dp.toPx()
          val usableW = (size.width - leftPad - 8.dp.toPx()).coerceAtLeast(1f)
          val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

          // Línea de 10% teórico
          val y10 = topPad + chartH * (1f - (10.0 / maxPct).toFloat().coerceIn(0f, 1f))
          drawLine(
            color = AmberWarning.copy(alpha = 0.65f),
            start = Offset(leftPad, y10),
            end = Offset(leftPad + usableW, y10),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
          )
          drawAxisText(
            textMeasurer = textMeasurer,
            text = "10%",
            topLeft = Offset(2.dp.toPx(), y10 - 6.dp.toPx()),
            color = AmberWarning,
            fontSize = 9.sp,
          )

          val groupW = usableW / 10f
          val barW = groupW * 0.34f
          val gap = groupW * 0.06f

          for (d in 0..9) {
            val hA = chartH * (yearAPcts[d] / maxPct).toFloat().coerceIn(0f, 1f)
            val hB = chartH * (yearBPcts[d] / maxPct).toFloat().coerceIn(0f, 1f)

            val xA = leftPad + d * groupW + (groupW - barW * 2 - gap) / 2f
            val xB = xA + barW + gap

            drawRoundRect(
              color = ElectricBlue,
              topLeft = Offset(xA, topPad + chartH - hA),
              size = Size(barW, hA),
              cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            )
            drawRoundRect(
              color = TurquoiseBright,
              topLeft = Offset(xB, topPad + chartH - hB),
              size = Size(barW, hB),
              cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            )
            drawAxisText(
              textMeasurer = textMeasurer,
              text = d.toString(),
              topLeft = Offset(xA + barW * 0.6f, topPad + chartH + 5.dp.toPx()),
              color = TextPrimary,
              fontSize = 11.sp,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color),
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = TextSecondary,
    )
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAxisText(
  textMeasurer: TextMeasurer,
  text: String,
  topLeft: Offset,
  color: Color,
  fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
) {
  drawText(
    textMeasurer = textMeasurer,
    text = text,
    topLeft = topLeft,
    style = TextStyle(
      fontFamily = JetBrainsMonoFamily,
      fontWeight = FontWeight.Medium,
      fontSize = fontSize,
      color = color,
    ),
  )
}
