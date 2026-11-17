package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.generator.ChanceModality
import com.example.domain.generator.GeneratedCombination
import com.example.domain.generator.GenerationStrategy
import com.example.domain.generator.HistoricalWindow
import com.example.ui.components.LotteryBallDisplay
import com.example.ui.components.StatisticalDisclaimerBanner
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DeepViolet
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
import com.example.ui.theme.TurquoiseContainer
import com.example.ui.theme.VioletContainer
import com.example.ui.theme.VioletLight
import com.example.ui.viewmodel.GeneratorUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
  uiState: GeneratorUiState,
  onModalitySelected: (ChanceModality) -> Unit,
  onStrategySelected: (GenerationStrategy) -> Unit,
  onLotterySelected: (String) -> Unit,
  onWindowSelected: (HistoricalWindow) -> Unit,
  onQuantityChanged: (Int) -> Unit,
  onGenerateClick: () -> Unit,
  onSaveFavoriteClick: (GeneratedCombination) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 840.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Generador de Jugadas (Chance)",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
          )
          Text(
            text = "Exploración combinatoria de 3 y 4 cifras conservando ceros iniciales.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
          )
        }
      }

      item {
        StatisticalDisclaimerBanner(
          title = "Exploración estadística — Sin garantía de predicción",
          body = "Los sorteos de lotería y Chance son eventos independientes. Ninguna estrategia histórica predice ni aumenta la probabilidad futura de acierto.",
          isWarningStyle = true,
        )
      }

      // Configuration Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
          border = BorderStroke(1.dp, BorderSubtle),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            // Modality
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Modalidad",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChanceModality.entries.forEach { modality ->
                  val selected = uiState.modality == modality
                  FilterChip(
                    selected = selected,
                    onClick = { onModalitySelected(modality) },
                    modifier = Modifier.testTag("modality_chip_${modality.digitsCount}"),
                    label = { Text(modality.label) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = ElectricBlueContainer,
                      selectedLabelColor = TurquoiseBright,
                    ),
                  )
                }
              }
            }

            // Reference Lottery & Historical Window
            SimpleDropdownSelector(
              label = "Lotería de referencia",
              selectedLabel = uiState.referenceLottery,
              options = uiState.availableLotteries,
              onSelect = onLotterySelected,
              testTag = "generator_lottery_dropdown",
            )

            SimpleDropdownSelector(
              label = "Período histórico de análisis",
              selectedLabel = uiState.historicalWindow.label,
              options = HistoricalWindow.entries.map { it.label },
              onSelect = { label ->
                HistoricalWindow.entries.firstOrNull { it.label == label }?.let(onWindowSelected)
              },
              testTag = "generator_window_dropdown",
            )

            // Strategy Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Estrategia de generación",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
              )
              GenerationStrategy.entries.forEach { strategy ->
                val selected = uiState.strategy == strategy
                Surface(
                  onClick = { onStrategySelected(strategy) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("strategy_option_${strategy.name.lowercase()}"),
                  shape = RoundedCornerShape(14.dp),
                  color = if (selected) VioletContainer.copy(alpha = 0.7f) else SurfaceNavyElevated,
                  border = BorderStroke(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) DeepViolet else BorderSubtle,
                  ),
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                  ) {
                    Text(
                      text = strategy.title,
                      style = MaterialTheme.typography.titleSmall,
                      color = if (selected) TurquoiseBright else TextPrimary,
                    )
                    Text(
                      text = strategy.description,
                      style = MaterialTheme.typography.bodySmall,
                      color = TextSecondary,
                    )
                  }
                }
              }
            }

            // Quantity Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "Cantidad de combinaciones",
                  style = MaterialTheme.typography.labelLarge,
                  color = TextSecondary,
                )
                Text(
                  text = "${uiState.quantity}",
                  fontFamily = JetBrainsMonoFamily,
                  fontWeight = FontWeight.Bold,
                  color = TurquoiseBright,
                )
              }
              Slider(
                value = uiState.quantity.toFloat(),
                onValueChange = { onQuantityChanged(it.roundToInt()) },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.testTag("generator_quantity_slider"),
                colors = SliderDefaults.colors(
                  thumbColor = TurquoiseBright,
                  activeTrackColor = ElectricBlue,
                ),
              )
            }

            Button(
              onClick = onGenerateClick,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_combinations_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            ) {
              Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Generar combinaciones (${uiState.modality.digitsCount} cifras)")
            }
          }
        }
      }

      if (uiState.generatedCombinations.isNotEmpty()) {
        item {
          Text(
            text = "Combinaciones generadas (${uiState.generatedCombinations.size})",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
          )
        }

        items(uiState.generatedCombinations, key = { it.id }) { combo ->
          val isSaved = "${combo.modality.digitsCount}:${combo.number}" in uiState.savedNumbersSet
          GeneratedCombinationCard(
            combination = combo,
            isSaved = isSaved,
            onSaveFavorite = { onSaveFavoriteClick(combo) },
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeneratedCombinationCard(
  combination: GeneratedCombination,
  isSaved: Boolean,
  onSaveFavorite: () -> Unit,
) {
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LotteryBallDisplay(
          number = combination.number,
          ballSize = 42.dp,
          accentColor = TurquoiseBright,
        )

        IconButton(
          onClick = onSaveFavorite,
          modifier = Modifier.testTag("save_favorite_${combination.number}"),
        ) {
          Icon(
            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = if (isSaved) "Guardada en favoritas" else "Guardar en favoritas",
            tint = if (isSaved) TurquoiseBright else ElectricBlueLight,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          color = TurquoiseContainer.copy(alpha = 0.7f),
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = combination.strategy.shortTag,
            style = MaterialTheme.typography.labelSmall,
            color = TurquoiseBright,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
        Surface(
          color = SurfaceNavyHighlight,
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = "${combination.referenceLottery} · n=${combination.sampleSizeUsed}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      // Positional digit frequency pills
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        combination.digitStats.forEach { stat ->
          Surface(
            color = SurfaceNavyHighlight,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BorderSubtle),
          ) {
            Text(
              text = "P${stat.positionIndex}: ${stat.digit} (${"%.1f".format(stat.positionalPct)}% · ${stat.tierLabel})",
              fontFamily = JetBrainsMonoFamily,
              fontSize = 11.sp,
              color = VioletLight,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Apariciones exactas pasadas: ${combination.exactHistoricalMatches}",
          style = MaterialTheme.typography.labelSmall,
          color = ElectricBlueLight,
        )
        Text(
          text = "Terminación ${combination.number.takeLast(2)}: ${combination.endingTwoDigitMatches}x",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary,
        )
      }

      Text(
        text = combination.justification,
        style = MaterialTheme.typography.bodySmall,
        color = TextPrimary.copy(alpha = 0.88f),
      )
    }
  }
}
